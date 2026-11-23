---
status: accepted
---
# Resource Type Sync Runs on a Dedicated Single-Thread Executor

## Context and Problem Statement

With `catalogue-lib.model.resource-type-sync.on-update=true`, updating a model also updates its mapped
registry resource type, which rebuilds the resource type's index fields and can take long. The user
who saves the model should not wait for that, so the update runs in the background.

The background work previously ran on an unbounded `SimpleAsyncTaskExecutor` registered as an
`AsyncTaskExecutor` bean. That bean:

* could stop Spring Boot from creating the host's `applicationTaskExecutor`, which is only
  auto-configured while no other `Executor` bean exists;
* could be injected by type into host code asking for an `Executor`;
* started a new thread per update with no bound, and could apply the refreshes of two quick updates of
  the same model out of order, leaving the resource type built from the older model.

Which executor should run the background resource type updates?

## Considered Options

* Update the resource type synchronously in the request
* Use the host's `applicationTaskExecutor`
* Keep a dedicated unbounded `SimpleAsyncTaskExecutor` bean
* Use a dedicated, bounded, single-thread `ThreadPoolTaskExecutor` that is not a default autowire candidate

## Decision Outcome

Chosen option: "a dedicated, bounded, single-thread `ThreadPoolTaskExecutor`", named
`catalogueResourceTypeSyncExecutor`, because it keeps the request responsive without affecting the
host's executors.

- One thread by default (`catalogue-lib.model.resource-type-sync.executor.pool-size`): updates are
  applied in the order they were submitted.
- Bounded queue (`...executor.queue-capacity`, default 100). When it is full, the update runs on the
  caller's thread: the request is delayed rather than the update dropped.
- Registered with `defaultCandidate = false` and after the task execution auto-configuration, so the
  host keeps its `applicationTaskExecutor` and by-type injection of `Executor` never resolves to it.
- Tasks run with the security context of the user who updated the model.
- On shutdown, queued updates are given up to 30 seconds to finish.

### Positive Consequences

* Model updates return without waiting for the resource type rebuild.
* Successive updates of a model reach the resource type in order.
* The host's executor configuration is unaffected.

### Negative Consequences

* A failed background update is only logged; the model and its resource type can drift until the
  model is saved again.
* Under sustained load beyond the queue capacity, model updates become synchronous.
* Updates of unrelated models are serialised behind each other on the single thread.
