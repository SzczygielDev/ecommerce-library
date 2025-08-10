package pl.szczygieldev.ecommercelibrary.outbox

interface Outbox {
    fun insertEvent(event: IntegrationEvent)
    fun insertEvents(events: List<IntegrationEvent>)
    fun markAsProcessed(event: IntegrationEvent)
    fun getEventsForProcessing(): List<OutboxMessage>
}