package pl.szczygieldev.ecommercelibrary.command

sealed class CommandStorageError(message: String) : CommandError(message)

data class CommandNotFoundError(override val message: String) : CommandStorageError(message) {
    companion object {
        fun forId(id: CommandId): CommandNotFoundError {
            return CommandNotFoundError("Cannot find command with id='${id.id}'.")
        }
    }
}

data class CommandAlreadyProcessingError(override val message: String) : CommandStorageError(message) {
    companion object {
        fun forId(id: CommandId): CommandAlreadyProcessingError {
            return CommandAlreadyProcessingError("Command with id='${id.id}' is processing or already been processed.")
        }
    }
}