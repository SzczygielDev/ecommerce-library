package pl.szczygieldev.ecommercelibrary.eventstore

import pl.szczygieldev.ecommercelibrary.ddd.core.DomainEvent
import pl.szczygieldev.ecommercelibrary.ddd.core.Identity

interface EventStore {
    fun appendEvents(aggregateId: Identity<*>, events: List<DomainEvent<*>>, exceptedVersion: Int)

    fun <T : DomainEvent<T>> getEvents(aggregateId: Identity<*>): List<T>?

    fun <T : DomainEvent<T>> getEventsForType(type: String ,offset: Int, limit: Int): List<T>
}