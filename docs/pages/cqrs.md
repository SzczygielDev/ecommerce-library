# CQRS

To simplify the implementation of the CQRS pattern, this library provides the following abstractions:

___ 

## Mediator Pattern
The main entry point for this CQRS implementation is the Mediator interface, which allows sending commands, queries, and
domain events.

### MediatorFacade
The base implementation that wraps the KediatR mediator library.

### ExposedTxPipelineBehaviour
Ensures atomic, transaction-bound operations for all commands processed through the mediator.

___ 

## Command side

### Command
All commands must extend the Command class. Each command defines an associated error type that may be returned if the
command fails.
Uses CommandId as its identifier.

### CommandError
All errors resulting from command execution must extend this class. It defines an error message and a code. Typically
used as a base for a hierarchy of command-related errors.

### CommandQueue
A specialized queue that enables asynchronous command processing using
the [MessageQueue](pages/messaging?id=messagequeue) as its core.
It operates in a push-based model.

### CommandProcessor
Responsible for executing commands pulled from the CommandQueue.

### CommandResult
Represents the result of command processing, including its status, timestamps for start and end, and any associated
errors.

**CommandResultStatus**
WAITING - Command is waiting to be processed by CommandProcessor
PROCESSING - Command is currently being processed.
SUCCESS - Command was successfully processed.
FAILURE - Command failed during processing.

CommandResultError describes any error encountered during command processing.

___ 

## Query side
The query side is currently fully based on the KediatR query mechanism, but it's encapsulated within the MediatorFacade.
___ 

## Event handling
To support a complete CQRS architecture, the library includes several event handler implementations. These are built on
top of KediatR’s notification mechanism.

### EventStore
The core interface for event store implementations. It allows:

- Appending events to streams
- Retrieving events by Identity
- Fetching events by type (for event handlers)
- Registering listeners

Two implementations are available:

- InMemoryEventStore
- ExposedEventStore

### InMemoryEventStore
A basic event store backed by an in-memory map. Appending events may throw an EventStoreLockingException if events are
appended with an older version than the current one.

### ExposedEventStore
A more advanced event store implementation using the Exposed library table as main storing mechanism.
Supports in-memory databases (e.g., H2) or persistent solutions. Throws EventStoreLockingException on version conflicts during appends.

### EventHandler
The base class for all event handler types. It's a facade over KediatR's NotificationHandler but enforces the use of the DomainEvent class.

### SyncEventHandler
A basic event handler type used to handle events synchronously in the same transaction as the command (e.g., for updating a read model with high consistency).

### AsyncEventHandler
Used when event handling doesn't need to occur in the same transaction.
Relies on MessageQueue for event storage and processing.

Two types are available:
- PollingAsyncEventHandler
- ReactiveAsyncEventHandler

### ReactiveEventHandler
An interface for event handler types that should be notified immediately when processing should begin.

### PollingAsyncEventHandler
A simple handler that periodically pulls events of a specific type from the event store and processes them in order of occurrence.
To start processing, call the processInternal() method. This design allows integration with different schedulers like Spring’s @Scheduled.

### ReactiveAsyncEventHandler
An advanced event handler that combines the behavior of PollingAsyncEventHandler with the ReactiveEventHandler interface.
It is registered as a listener in the event store and processes events as soon as they arrive. Handlers of this type are automatically registered upon instantiation via constructor injection.