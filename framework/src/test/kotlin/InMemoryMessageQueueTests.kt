import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
import pl.szczygieldev.ecommercelibrary.messaging.*
import pl.szczygieldev.ecommercelibrary.messaging.config.MessageQueueConfig

class InMemoryMessageQueueTests : FunSpec() {

    init {
        isolationMode = IsolationMode.InstancePerLeaf
        val maxRetries = 3
        val config = MessageQueueConfig(maxRetries)
        val queue = InMemoryMessageQueue<String>(config)

        test("Pushed message on queue should be stored") {
            // Arrange
            val message = Message(payload = "some payload", timestamp = Clock.System.now())
            val messageId = message.id

            // Act
            queue.push(message)

            // Assert
            val found = queue.getMessage(messageId)
            found.shouldNotBeNull()
        }

        test("Pushing message multiple times should be idempotent") {
            // Arrange
            val message = Message(payload = "some payload", timestamp = Clock.System.now())
            val messageId = message.id

            // Act
            queue.push(message)
            queue.push(message)
            queue.push(message)

            queue.pull()

            // Assert
            queue.isEmpty().shouldBeTrue()
        }

        test("Pulling message should remove it from queue") {
            // Arrange
            val message = Message(payload = "some payload", timestamp = Clock.System.now())

            // Act
            queue.push(message)
            queue.pull()
            val secondPull = queue.pull()

            // Assert
            secondPull.shouldBeNull()
        }

        test("Pulling message should change it status to PROCESSING") {
            // Arrange
            val message = Message(payload = "some payload", timestamp = Clock.System.now())
            val messageId = message.id

            // Act
            queue.push(message)
            queue.pull()
            val messageFromQueue = queue.getMessage(messageId)

            // Assert
            messageFromQueue.shouldNotBeNull()
            messageFromQueue.state.status.shouldBe(MessageProcessingStatus.PROCESSING)
        }

        test("ACK should change processing status to SUCCESS") {
            // Arrange
            val message = Message(payload = "some payload", timestamp = Clock.System.now())
            val messageId = message.id

            // Act
            queue.push(message)
            val pulledMessage = queue.pull()
            queue.ack(pulledMessage!!)
            val messageFromQueue = queue.getMessage(messageId)

            // Assert
            messageFromQueue.shouldNotBeNull()
            messageFromQueue.state.status.shouldBe(MessageProcessingStatus.SUCCESS)
        }

        test("Requeuing should put message back on queue with status WAITING") {
            // Arrange
            val message = Message(payload = "some payload", timestamp = Clock.System.now())
            val messageId = message.id
            val processingError = MessageProcessingError("some error", "error description")

            // Act
            queue.push(message)
            val pulledMessage = queue.pull()
            queue.requeue(pulledMessage!!, processingError)
            val messageFromQueue = queue.getMessage(messageId)

            // Assert
            messageFromQueue.shouldNotBeNull()
            messageFromQueue.state.status.shouldBe(MessageProcessingStatus.WAITING)
        }

        test("Requeuing should increase error count by 1 and update error list") {
            // Arrange
            val message = Message(payload = "some payload", timestamp = Clock.System.now())
            val messageId = message.id
            val processingError = MessageProcessingError("some error", "error description")

            // Act
            queue.push(message)
            val pulledMessage = queue.pull()
            queue.requeue(pulledMessage!!, processingError)
            val messageFromQueue = queue.getMessage(messageId)

            // Assert
            messageFromQueue.shouldNotBeNull()
            messageFromQueue.state.failCount.shouldBe(1)
            messageFromQueue.state.errors.size shouldBe 1
        }

        test("Requeued message should be put on DLQ when fail count exceeds") {
            // Arrange
            val message = Message(payload = "some payload", timestamp = Clock.System.now())
            val messageId = message.id
            val processingError = MessageProcessingError("some error", "error description")

            // Act
            queue.push(message)

            var pulledMessage = queue.pull()
            queue.requeue(pulledMessage!!, processingError)

            pulledMessage = queue.pull()
            queue.requeue(pulledMessage!!, processingError)

            pulledMessage = queue.pull()
            queue.requeue(pulledMessage!!, processingError)

            val messageFromQueue = queue.getMessage(messageId)

            // Assert
            messageFromQueue.shouldNotBeNull()
            messageFromQueue.state.status.shouldBe(MessageProcessingStatus.FAILURE)
            messageFromQueue.state.failCount.shouldBe(3)
            messageFromQueue.state.errors.size shouldBe 3
        }
    }
}