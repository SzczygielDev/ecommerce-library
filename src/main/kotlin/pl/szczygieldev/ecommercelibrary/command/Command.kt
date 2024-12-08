package pl.szczygieldev.ecommercelibrary.command

import arrow.core.Either
import com.trendyol.kediatr.CommandWithResult

//TODO - for now its only external dependency in application, in future should be removed
abstract class Command<TError :CommandError>(val id: CommandId = CommandId()) : CommandWithResult<Either<TError, Unit>>