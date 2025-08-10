package pl.szczygieldev.ecommercelibrary.command

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import pl.szczygieldev.ecommercelibrary.messaging.Message
import pl.szczygieldev.ecommercelibrary.messaging.MessageId
import pl.szczygieldev.ecommercelibrary.messaging.MessageQueue
import pl.szczygieldev.ecommercelibrary.messaging.MessageProcessingStatus
import java.time.Duration
import kotlin.time.toJavaDuration

class CommandQueue(private val queue: MessageQueue<Command<*>>) {
    suspend fun push(command: Command<*>) {
        queue.push(Message(id = MessageId(command.id.id), payload = command, timestamp = Clock.System.now()))
    }

    fun getCommandStatus(commandId: CommandId): CommandResult? {
        val found = queue.getMessage(MessageId(commandId.id)) ?: return null
        val foundCommand = found.payload

        val status = when (found.state.status) {
            MessageProcessingStatus.WAITING -> CommandResultStatus.WAITING
            MessageProcessingStatus.PROCESSING -> CommandResultStatus.PROCESSING
            MessageProcessingStatus.SUCCESS -> CommandResultStatus.SUCCESS
            MessageProcessingStatus.FAILURE -> CommandResultStatus.FAILURE
        }

        return CommandResult(
            foundCommand.id,
            foundCommand,
            status,
            found.timestamp.toJavaInstant(),
            found.state.finishTimestamp?.minus(found.state.beginTimestamp)?.toJavaDuration() ?: Duration.ZERO,
            found.state.errors.map { CommandResultError(it.name, it.message) }.toMutableList()
        )
    }
}