package pl.szczygieldev.ecommercelibrary.outbox

import pl.szczygieldev.ecommercelibrary.ddd.core.DomainEvent

interface Outbox {
    fun insertEvent(event: DomainEvent<*>)
    fun insertEvents(events: List<DomainEvent<*>>)
    fun markAsProcessed(event: DomainEvent<*>)
    fun getEventsForProcessing(): List<OutboxMessage>
}