package pl.szczygieldev.ecommercelibrary.command

interface CommandResultStorage {
    fun <T: CommandError> commandBegin(command: Command<T>)
    fun <T: CommandError> commandFailed(id: CommandId, error: T)
    fun commandSuccess(id: CommandId)
    fun findById(id: CommandId): CommandResult?
    fun findAll(): List<CommandResult>
}

