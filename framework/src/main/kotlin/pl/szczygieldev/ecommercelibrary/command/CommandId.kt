package pl.szczygieldev.ecommercelibrary.command

import java.util.UUID

data class CommandId(val id: String = UUID.randomUUID().toString())