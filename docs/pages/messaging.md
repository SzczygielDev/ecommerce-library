# Messaging
This library includes messaging abstractions that help you start a module quickly without worrying about messaging implementation details or external dependencies.

## Message
Message is the primary data structure used by the messaging component of the library. It represents a single message along with its payload.

### MessageId
Identifies a message uniquely.

### MessageProcessingState
Represents the processing state of a message, including its status, the number of failures, associated error messages, and timestamps for when processing started and finished.

## MessageQueue
MessageQueue is the main abstraction of a message queue. It provides capabilities to:
- Push messages onto the queue
- Pull messages from the queue
- Acknowledge (ACK) received messages
- Requeue messages when processing fails
- Retrieve messages by ID, regardless of their processing status
- Check if the queue is empty
- Check the size of the Dead Letter Queue (DLQ)
- Register listeners for incoming messages


## InMemoryMessageQueue
The primary implementation of the MessageQueue interface. It uses in-memory concurrent collections and supports all features defined by the interface. It also includes deduplication capabilities.

## MessageQueueListener
MessageQueueListener is an abstraction for classes that should listen to specific messages arriving on the queue.

