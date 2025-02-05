package pl.szczygieldev.ecommercelibrary.messaging

import java.util.*

data class MessageId(val id: String = UUID.randomUUID().toString())