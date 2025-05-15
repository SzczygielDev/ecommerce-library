# Outbox Pattern
To improve integration reliability and maintain transactional boundaries in event propagation, this library offers a simple implementation of the Outbox pattern.

## Outbox
The core component is the Outbox class, which serves as an abstraction for implementing the Outbox pattern.

## InMemoryOutbox
InMemoryOutbox is a basic implementation that uses an in-memory collection to store outbox messages.

## OutboxBackgroundWorker
OutboxBackgroundWorker is responsible for periodically fetching messages from the outbox, mapping them into integration events, and invoking the provided callback via the onPublish method. The default polling interval is 1000 ms.

## OutboxMessage
OutboxMessage is a data structure that stores all the necessary information required to process an event through the outbox.

## OutboxMessageStatus
OutboxMessageStatus indicates the current processing state of an outbox message.

## StoreAndForwardEventPublisher
StoreAndForwardEventPublisher enables transactional publishing of domain events using the outbox. It also ensures that all event handlers within the same bounded context or module are invoked using mediator.

## StoreAndForwardApplicationEventPublisher
StoreAndForwardApplicationEventPublisher is an event publisher that stores events transactionally in the outbox and also publishes them using Spring’s ApplicationEventPublisher.