package pl.szczygieldev.ecommercelibrary.command

import arrow.core.Either
import arrow.core.raise.either
import java.time.Duration
import java.time.Instant

class InMemoryCommandResultStorage : CommandResultStorage {
    private val db = mutableMapOf<CommandId, CommandResult>()

    override fun findById(id: CommandId): CommandResult? = db[id]
    override fun findAll(): List<CommandResult> = db.values.toList()

    override fun commandBegin(command: Command): Either<CommandError, Unit>  = either {
        if(db.containsKey(command.id)){
            raise(CommandAlreadyProcessingError.forId(command.id))
        }

        db.put(
            command.id,
            CommandResult(command.id,command, CommandResultStatus.RUNNING, Instant.now(), Duration.ZERO, mutableListOf())
        )
    }

    override fun commandFailed(id: CommandId, error: CommandError): Either<CommandError,Unit> = either {
        val foundCommand = findById(id) ?: raise(CommandNotFoundError.forId(id))

        foundCommand.status = CommandResultStatus.ERROR
        foundCommand.duration = Duration.between(foundCommand.timestamp, Instant.now())
        foundCommand.errors.add(CommandResultError(error.javaClass.name, error.message))
    }

    override fun commandFailed(id: CommandId, errors: List<CommandError>) : Either<CommandError,Unit> = either{
        val foundCommand = findById(id) ?: raise(CommandNotFoundError.forId(id))

        foundCommand.status = CommandResultStatus.ERROR
        foundCommand.duration = Duration.between(foundCommand.timestamp, Instant.now())
        foundCommand.errors.addAll(errors.map { error -> CommandResultError(error.javaClass.name, error.message) })
    }

    override fun commandSuccess(id: CommandId) : Either<CommandError,Unit> = either{
        val foundCommand = findById(id) ?: raise(CommandNotFoundError.forId(id))

        foundCommand.status = CommandResultStatus.SUCCESS
        foundCommand.duration = Duration.between(foundCommand.timestamp, Instant.now())
    }
}