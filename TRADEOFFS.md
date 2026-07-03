# Trade-offs

The implementation intentionally favors explicit coroutine-based coordination and feature isolation over framework abstractions. The following sections summarize the main architectural decisions and the reasoning behind them.

---

## 1. Signing Coordination: CompletableDeferred vs Alternatives

**The problem:** a feature processor needs to suspend mid-coroutine, hand off to a signing screen, and resume execution with the result.

**Alternatives considered:**

*Navigation arguments + SavedStateHandle* — pass the challenge as a nav argument, write the result back via `SavedStateHandle`. Simple, but it splits a single logical operation across two nav events and makes the feature processor poll for a result rather than suspend for one. Error handling becomes awkward.

*Shared ViewModel scoped to the nav graph* — a single ViewModel accessible to both the feature screen and the signing screen. Works, but tightly couples independent features through a shared scope and makes it unclear who owns the signing lifecycle.

*Global event bus (SharedFlow)* — emit a signing request event, collect the result event. Decoupled, but there is no type-safe link between a specific request and its result. Under concurrency, matching the right result to the right request requires manual correlation.

**Chosen: `CompletableDeferred` inside `SigningCoordinator`.** The coroutine model naturally represents the transaction flow as a single sequential operation:

```
fetchQuotation()
       ↓
requestSigning()    ← suspends here
       ↓
submitTransaction() ← resumes execution here
```

without callbacks, shared mutable state, or navigation-specific result APIs.

---

## 2. Navigation Ownership

**Alternative:** navigate directly from processors by exposing navigation events as side effects.

This would allow each feature processor to control when the signing screen is opened, but it couples business logic to navigation and makes processors dependent on UI concepts.

**Chosen:** processors communicate only with `SigningCoordinator`.

`AppNavHost` becomes a pure observer of coordinator state rather than an owner of signing state. This keeps processors completely UI-agnostic while allowing the signing flow to be reused by any feature without introducing navigation dependencies.

---

## 3. Coordinator State: `StateFlow` vs `Channel`

**Alternative: `Channel<PendingSigning>`** — emit each signing request as a one-shot event. `AppNavHost` collects the channel and stores the current pending in local `mutableStateOf`. This works but splits ownership: the coordinator produces the request while `AppNavHost` holds the state.

**Chosen: `StateFlow<PendingSigning?>`** — the coordinator owns both the request and its lifecycle in one place. `AppNavHost` observes it with `collectAsStateWithLifecycle()` and needs no local copy. The "what is currently pending" question is answered by the coordinator, not the UI layer.

---

## 4. MVI: Custom vs Library

**Alternatives:** Orbit MVI, MVI Kotlin, and similar libraries provide reducers, bootstrappers, and structured side effect pipelines out of the box.

**Chosen: custom lightweight MVI** (`Processor<I, S, E>` base class with `StateFlow` + `Channel`). A library would abstract away the coroutine coordination that the assignment is specifically evaluating. The custom base class is intentionally minimal and only standardizes intent handling, state exposure, and side effects.

---

## 5. Error Handling: `Result<T>` vs Exceptions

**Alternative:** repositories throw exceptions; processors catch with `try/catch`. Common, but broad `catch (e: Exception)` blocks risk swallowing `CancellationException`.

**Chosen: `Result<T>` return types.** Repository implementations explicitly rethrow `CancellationException` before catching anything else. Processors use `getOrElse { return@launch }` with no try/catch in business logic.

Preserving cancellation was particularly important because the signing flow suspends processors while awaiting user interaction. Accidentally swallowing `CancellationException` could leave suspended operations running after their parent scope had already been cancelled.

---

## 6. Feature Organization

**Alternative:** separate layers (`data/`, `domain/`, `presentation/`) across the entire project. This scales well for large applications but can make it harder to understand small, self-contained features.

**Chosen: feature-based packages.** Each transaction feature owns its processor, repository, and UI while shared infrastructure remains under `core/`. This improves discoverability and mirrors the structure of the assignment itself. The decision to avoid classic Clean Architecture layering was intentional — the added abstraction would not have served a project of this scope.
