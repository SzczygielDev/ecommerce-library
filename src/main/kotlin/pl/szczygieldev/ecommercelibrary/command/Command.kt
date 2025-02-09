package pl.szczygieldev.ecommercelibrary.command

import arrow.core.Either
import com.trendyol.kediatr.CommandWithResult

abstract class Command<TError :CommandError>(val id: CommandId = CommandId()) : CommandWithResult<Either<TError, Unit>>