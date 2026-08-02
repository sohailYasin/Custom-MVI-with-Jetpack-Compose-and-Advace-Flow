# Custom-MVI-with-Jetpack-Compose-and-Advace-Flow

Reusable Android transaction-signing flow built with Kotlin, Jetpack Compose, custom MVI, Coroutines, StateFlow, Hilt, and unit tests.

## Overview

When a user initiates a transaction, the feature processor fetches a quotation, suspends while the user authenticates on a shared signing screen, and resumes with the resulting signature to submit the transaction. The signing flow is a single shared feature triggered from any transaction screen.

## Architecture

The key design question — how does a feature processor suspend mid-coroutine and resume with a result from a separate screen — is solved using `SigningCoordinator`, a `@Singleton` that holds a `StateFlow<PendingSigning?>`. Each signing request bundles a `SigningRequest` with a `CompletableDeferred<SigningResult>`. The processor suspends by awaiting the deferred; the signing screen resolves it when the user authenticates or cancels.

See [`ARCHITECTURE.md`](ARCHITECTURE.md) for a full walkthrough and flow diagram, and [`TRADEOFFS.md`](TRADEOFFS.md) for the reasoning behind key design decisions.

## Tech Stack

- **Kotlin** + **Coroutines** (`StateFlow`, `Channel`, `CompletableDeferred`)
- **Jetpack Compose** + **Navigation Compose**
- **Hilt** for dependency injection
- **MVI** via a custom `Processor<Intent, UiState, SideEffect>` base class
- **JUnit 4** + **kotlinx-coroutines-test** for unit tests

## Building

Requires Android Studio Hedgehog or newer, JDK 17, and Android SDK 35.

```bash
./gradlew assembleDebug
```

To run unit tests:

```bash
./gradlew test
```

## Project Structure

```
core/
  model/    — shared domain models (SigningRequest, SigningResult, OperationType)
  mvi/      — Processor base class, Intent and UiState markers
  signing/  — SigningCoordinator
di/         — Hilt module binding repository interfaces to implementations
signing/    — SigningProcessor, SigningScreen and related MVI files
views/
  home/       — HomeScreen
  withdrawal/ — full MVI + repository implementation
  transfer/   — full MVI + repository implementation
  swap/       — full MVI + repository implementation
```
