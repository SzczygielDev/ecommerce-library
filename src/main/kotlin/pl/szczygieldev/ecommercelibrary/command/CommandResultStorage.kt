package pl.szczygieldev.ecommercelibrary.command

import arrow.core.Either

interface CommandResultStorage {
    fun commandBegin(command: Command): Either<CommandError,Unit>
    fun commandFailed(id: CommandId, error: CommandError): Either<CommandError,Unit>
    fun commandFailed(id: CommandId, errors: List<CommandError>): Either<CommandError,Unit>
    fun commandSuccess(id: CommandId): Either<CommandError,Unit>
    fun findById(id: CommandId): CommandResult?
    fun findAll(): List<CommandResult>
}

