# Architecture

## Overview

The app implements a Transaction Signing Flow across three operations — Withdrawal, Transfer, and Swap — each of which requires the user to review and sign a cryptographic challenge before the transaction is submitted. A single `SigningScreen` is shared across all three operations.

---

## Design Goals

The implementation was guided by four design goals:

- Keep feature processors independent of navigation and UI.
- Make the signing flow reusable across multiple transaction types.
- Preserve a sequential coroutine flow without callbacks.
- Allow future signing providers (e.g. wallet integrations) without changing feature processors.

---

## The Signing Coordination Problem

When a user initiates a withdrawal, the `WithdrawalProcessor` must:

1. Fetch a transaction quotation
2. **Suspend** while the user reviews and signs the challenge on an independent feature screen
3. Resume execution with the signing result and submit the transaction

The challenge is suspending a feature processor mid-coroutine and resuming it with a result produced by a completely independent feature — without coupling the two through navigation APIs, callbacks, or shared mutable state.

---

## Solution: SigningCoordinator

`SigningCoordinator` is a `@Singleton` that owns the entire request/response lifecycle for signing.

Each signing request is represented by a `PendingSigning` object containing the immutable `SigningRequest` and a dedicated `CompletableDeferred<SigningResult>`. The processor suspends by awaiting this deferred, and the signing flow completes it once the user signs or cancels.

`AppNavHost` observes the current signing request and performs navigation when a new request appears. This keeps navigation outside of business logic while allowing processors to remain completely UI-agnostic.

### Flow

```
┌───────────────────────────┐
│    WithdrawalProcessor    │
│  handleContinue()         │
└────────────┬──────────────┘
             │ requestSigning()
             ▼
┌───────────────────────────┐
│    SigningCoordinator     │
│  creates PendingSigning   │
│  currentSigning = pending │
└────────────┬──────────────┘
             │ StateFlow emission
             ▼
┌───────────────────────────┐
│        AppNavHost         │
│  observes currentSigning  │
│  navigate("signing")      │
└────────────┬──────────────┘
             │
             ▼
┌───────────────────────────┐
│      SigningScreen        │
│  user reviews & signs     │
└────────────┬──────────────┘
             │ onDeliver(result)
             ▼
  pending.deferred.complete(result)
             │
             ▼
┌───────────────────────────┐
│    WithdrawalProcessor    │
│  resumes execution        │
│  submitTransaction()      │
└───────────────────────────┘
```

---

## Why CompletableDeferred?

Several approaches were considered for returning a signing result to the originating processor.

`CompletableDeferred` was chosen because it naturally models a one-to-one request/response interaction. It allows a processor to suspend using normal sequential coroutine code:

```
fetchQuotation()
       ↓
requestSigning()    ← suspends here
       ↓
submitTransaction() ← resumes execution here
```

without introducing callbacks, shared mutable state, or navigation-specific result APIs. The `CompletableDeferred` is bundled with its `SigningRequest` in a single `PendingSigning` object, which ensures a result can never be delivered to the wrong request if the code evolves to support concurrent operations.

---

## MVI Pattern

Each feature follows a lightweight MVI pattern with three components:

| Component | Role |
|---|---|
| `Processor<I, S, E>` | ViewModel subclass. Receives intents, updates state, emits side effects. |
| `Intent` | Sealed class representing user actions (e.g. `WithdrawalIntent.Continue`). |
| `UiState` | Immutable data class held in `StateFlow`. Screen observes and renders it. |
| `SideEffect` | One-shot events emitted via `Channel` for navigation and other non-state outcomes. |

Screens collect `state` with `collectAsStateWithLifecycle()` and observe `sideEffect` in a `LaunchedEffect`.

---

## Repository Layer

Each feature has a repository interface with `Result<T>` return types. Processors use `getOrElse { return@launch }` to handle failures without try/catch blocks. Mock repositories always rethrow `CancellationException` so coroutine cancellation propagates correctly and structured concurrency is preserved.

---

## Extensibility

The signing flow was intentionally isolated behind `SigningCoordinator`. Supporting a real wallet provider (for example Reown AppKit) would primarily require replacing the mock signing implementation inside the Signing feature without changing processors or transaction flows.

---

## Package Structure

```
core/
  model/          — shared domain models (SigningRequest, SigningResult, OperationType)
  mvi/            — Processor base class, Intent and UiState markers
  signing/        — SigningCoordinator (shared infrastructure)
di/               — Hilt module binding repository interfaces to implementations
signing/          — SigningProcessor, SigningScreen and related MVI files
views/
  home/           — HomeScreen (no processor; pure navigation)
  withdrawal/     — WithdrawalProcessor, WithdrawalRepository, WithdrawalScreen
  transfer/       — TransferProcessor, TransferRepository, TransferScreen
  swap/           — SwapProcessor, SwapRepository, SwapScreen
```
