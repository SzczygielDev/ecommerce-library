package pl.szczygieldev.ecommercelibrary.command

import arrow.core.Either

interface Mediator {
    suspend fun <T: CommandError> send(command: Command<T>): Either<T, Unit>
}