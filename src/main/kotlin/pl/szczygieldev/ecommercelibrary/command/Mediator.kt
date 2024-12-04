package pl.szczygieldev.ecommercelibrary.command

import arrow.core.Either

interface Mediator {
    suspend fun send(command: Command): Either<CommandError, Unit>

    suspend fun sendAsync(command: Command)
}