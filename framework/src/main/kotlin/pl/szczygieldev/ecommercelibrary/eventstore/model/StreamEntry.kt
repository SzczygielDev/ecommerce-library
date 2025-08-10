package pl.szczygieldev.ecommercelibrary.eventstore.model

import pl.szczygieldev.ecommercelibrary.ddd.core.Identity

data class StreamEntry(val aggregateId: Identity<*>, val version: Int, val eventType: String, val eventData: String)
