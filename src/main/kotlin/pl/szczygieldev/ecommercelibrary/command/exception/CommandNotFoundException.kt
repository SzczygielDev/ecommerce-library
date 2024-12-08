package pl.szczygieldev.ecommercelibrary.command.exception

import pl.szczygieldev.ecommercelibrary.command.CommandId

class CommandNotFoundException(val id: CommandId) : RuntimeException("Cannot find command with id='${id.id}'.")