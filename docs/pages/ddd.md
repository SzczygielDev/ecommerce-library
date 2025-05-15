# Domain-Driven Design
This library provides few building blocks of tactical Domain-Driven Design to make domain code more explicit and unified.

## @AggregateRoot
A flag annotation used to designate aggregate roots, making it easier to navigate between aggregates in the system.

## DomainEvent
DomainEvent is the base class for all events in the domain layer. It includes an ID, a timestamp indicating when the event occurred, and supports easy comparison.

## DomainEventPublisher
DomainEventPublisher is an abstraction for publishing domain events within a bounded context. For publishing events outside the bounded context, consider using an [Outbox Pattern](/pages/outbox.md).

## @DomainService
A flag annotation used to identify domain services.

## Entity
Entity is the base class for all entities or domain objects that have a lifecycle.

## EventSourcedEntity
EventSourcedEntity extends the Entity class and adds support for event sourcing. It includes:
- Storing all events that have occurred on the entity
- Tracking newly occurred events after rebuilding the entity
- Versioning
- Raising events within the entity
- applyAll method for rebuilding the entity from events
- An abstraction for implementing state mutators

## Identity
Identity is an abstraction used to uniquely identify domain objects, simplifying comparisons.

## UuidIdentity
UuidIdentity extends Identity and uses UUIDs as the primary identifier.