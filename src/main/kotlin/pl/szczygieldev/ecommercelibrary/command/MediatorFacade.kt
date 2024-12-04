package pl.szczygieldev.ecommercelibrary.command

import arrow.core.Either
import com.trendyol.kediatr.Mediator
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MediatorFacade(val kediatr: Mediator, val commandResultStorage: CommandResultStorage) :
    pl.szczygieldev.ecommercelibrary.command.Mediator {
    private val log = KotlinLogging.logger(javaClass.name)
    private val coroutineScope =
        CoroutineScope(Job() + CoroutineExceptionHandler { context, throwable -> log.error { "Exception while processing command in background: $throwable" } })

    override suspend fun send(command: Command): Either<CommandError, Unit> {
        commandResultStorage.commandBegin(command)

        val result = kediatr.send(command)

        result.fold({
            handleFailure(command, it)
        }, {
            handleSuccess(command)
        })

        return result
    }

    override suspend fun sendAsync(command: Command) {
        commandResultStorage.commandBegin(command)

        coroutineScope.launch {
            val result = kediatr.send(command)

            result.fold({
                handleFailure(command, it)
            }, {
                handleSuccess(command)
            })
        }
    }

    private fun handleSuccess(command: Command) {
        log.info { "Command with id='${command.id}' finished successfully" }
        commandResultStorage.commandSuccess(command.id)
    }

    private fun handleFailure(command: Command, error: CommandError) {
        log.error { "Error while processing command with id='${command.id}' error='$error'" }
        commandResultStorage.commandFailed(command.id, error)
    }
}