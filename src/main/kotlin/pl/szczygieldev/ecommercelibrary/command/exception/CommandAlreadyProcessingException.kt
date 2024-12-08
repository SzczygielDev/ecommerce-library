package pl.szczygieldev.ecommercelibrary.command.exception

import pl.szczygieldev.ecommercelibrary.command.CommandId

class CommandAlreadyProcessingException(val id: CommandId) : RuntimeException("Command with id='${id.id}' is processing or already been processed.")