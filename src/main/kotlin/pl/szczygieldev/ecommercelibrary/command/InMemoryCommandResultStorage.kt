package pl.szczygieldev.ecommercelibrary.command

import pl.szczygieldev.ecommercelibrary.command.exception.CommandAlreadyProcessingException
import pl.szczygieldev.ecommercelibrary.command.exception.CommandNotFoundException
import java.time.Duration
import java.time.Instant

class InMemoryCommandResultStorage : CommandResultStorage {
    private val db = mutableMapOf<CommandId, CommandResult>()

    override fun findById(id: CommandId): CommandResult? = db[id]
    override fun findAll(): List<CommandResult> = db.values.toList()

    override fun <T: CommandError> commandBegin(command: Command<T>) {
        if(db.containsKey(command.id)){
            throw CommandAlreadyProcessingException(command.id)
        }

        db.put(
            command.id,
            CommandResult(command.id,command, CommandResultStatus.RUNNING, Instant.now(), Duration.ZERO, mutableListOf())
        )
    }

    override fun <T: CommandError> commandFailed(id: CommandId, error: T){
        val foundCommand = findById(id) ?: throw CommandNotFoundException(id)

        foundCommand.status = CommandResultStatus.ERROR
        foundCommand.duration = Duration.between(foundCommand.timestamp, Instant.now())
        foundCommand.errors.add(CommandResultError(error.javaClass.name, error.message))
    }

    override fun commandSuccess(id: CommandId) {
        val foundCommand = findById(id) ?: throw CommandNotFoundException(id)

        foundCommand.status = CommandResultStatus.SUCCESS
        foundCommand.duration = Duration.between(foundCommand.timestamp, Instant.now())
    }
}