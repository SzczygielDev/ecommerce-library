package pl.szczygieldev.ecommercelibrary.command

import arrow.core.Either
import com.trendyol.kediatr.Mediator

class MediatorFacade(val kediatr: Mediator) :
    pl.szczygieldev.ecommercelibrary.command.Mediator {

    override suspend fun <T : CommandError> send(command: Command<T>): Either<T, Unit> {
        return kediatr.send(command)
    }
}