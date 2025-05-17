package pl.szczygieldev.ecommercelibrary.outbox

import com.fasterxml.jackson.databind.ObjectMapper

class InMemoryOutbox(val objectMapper: ObjectMapper) : Outbox {
    private val db = mutableSetOf<OutboxMessage>()
    override fun insertEvent(event: IntegrationEvent) {
        db.add(
            OutboxMessage(
                event.id,
                OutboxMessageStatus.PENDING,
                objectMapper.writeValueAsString(event),
                event.javaClass.typeName,
                event.occurredOn
            )
        )
    }

    override fun insertEvents(events: List<IntegrationEvent>) {
        events.forEach { event ->
            insertEvent(event)
        }
    }

    override fun markAsProcessed(event: IntegrationEvent) {
        db.find { message -> message.eventId == event.id }?.status = OutboxMessageStatus.PROCESSED
    }

    override fun getEventsForProcessing(): List<OutboxMessage> =
        db.filter { message -> message.status == OutboxMessageStatus.PENDING }
}