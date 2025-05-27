import arrow.core.Either
import arrow.core.raise.either
import com.trendyol.kediatr.CommandWithResultHandler
import com.trendyol.kediatr.MappingDependencyProvider.Companion.createMediator
import io.kotest.assertions.nondeterministic.until
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.delay
import pl.szczygieldev.ecommercelibrary.command.*
import pl.szczygieldev.ecommercelibrary.messaging.InMemoryMessageQueue
import pl.szczygieldev.ecommercelibrary.messaging.config.MessageQueueConfig
import kotlin.time.Duration.Companion.seconds

class CommandProcessorTests : FunSpec() {
    private class SuccessCommand : Command<CommandError>()
    private class FailingWithErrorCommand : Command<CommandError>()
    private class FailingWithExceptionCommand : Command<CommandError>()

    private class SuccessCommandHandler :
        CommandWithResultHandler<SuccessCommand, Either<CommandError, Unit>> {

        override suspend fun handle(command: SuccessCommand): Either<CommandError, Unit> = either {
            // do nothing
        }
    }

    private class FailingWithErrorCommandHandler :
        CommandWithResultHandler<FailingWithErrorCommand, Either<CommandError, Unit>> {

        override suspend fun handle(command: FailingWithErrorCommand): Either<CommandError, Unit> = either {
            raise(object : CommandError("some error","CODE-0") {})
        }
    }

    private class FailingWithExceptionCommandHandler :
        CommandWithResultHandler<FailingWithExceptionCommand, Either<CommandError, Unit>> {

        override suspend fun handle(command: FailingWithExceptionCommand): Either<CommandError, Unit> = either {
            throw Exception("some exception")
        }
    }

    init {
        val kediatr = createMediator(
            handlers = listOf(
                SuccessCommandHandler(),
                FailingWithErrorCommandHandler(),
                FailingWithExceptionCommandHandler()
            )
        )
        val mediator: Mediator = MediatorFacade(kediatr)
        val queue = InMemoryMessageQueue<Command<*>>(MessageQueueConfig())
        val cmdQueue = CommandQueue(queue)
        val processor = CommandProcessor(mediator, queue)

        test("Command should have SUCCESS status when no error or exceptions occurred") {
            queue.registerListener(processor)
            val cmd = SuccessCommand()

            cmdQueue.push(cmd)

            until(3.seconds) {
                val status = cmdQueue.getCommandStatus(cmd.id)

                status != null &&
                        status.status == CommandResultStatus.SUCCESS
            }
        }

        test("Command should have FAILURE status when error occurred while processing") {
            queue.registerListener(processor)
            val cmd = FailingWithErrorCommand()

            cmdQueue.push(cmd)

            until(3.seconds) {
                val status = cmdQueue.getCommandStatus(cmd.id)

                status != null &&
                        status.status == CommandResultStatus.FAILURE
            }

        }

        test("Command should have FAILURE status when exception occurred while processing") {
            queue.registerListener(processor)
            val cmd = FailingWithExceptionCommand()

            cmdQueue.push(cmd)

            until(3.seconds) {
                val status = cmdQueue.getCommandStatus(cmd.id)

                status != null &&
                        status.status == CommandResultStatus.FAILURE
            }
        }
    }
}