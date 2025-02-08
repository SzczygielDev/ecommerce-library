package pl.szczygieldev.ecommercelibrary.command

import arrow.core.Either
import com.trendyol.kediatr.Query

interface Mediator {
    suspend fun <T: CommandError> send(command: Command<T>): Either<T, Unit>

    suspend fun <TQuery : Query<TResponse>, TResponse> send(query: TQuery): TResponse
}