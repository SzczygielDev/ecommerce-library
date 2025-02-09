package pl.szczygieldev.ecommercelibrary.command

import arrow.core.Either
import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.Query

class MediatorFacade(val kediatr: Mediator) :
    pl.szczygieldev.ecommercelibrary.command.Mediator {

    override suspend fun <T : CommandError> send(command: Command<T>): Either<T, Unit> {
        return kediatr.send(command)
    }

    override suspend fun <TQuery : Query<TResponse>, TResponse> send(query: TQuery): TResponse {
        return kediatr.send(query)
    }
}