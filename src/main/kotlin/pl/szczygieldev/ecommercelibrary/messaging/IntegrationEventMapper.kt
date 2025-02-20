package pl.szczygieldev.ecommercelibrary.messaging

import pl.szczygieldev.ecommercelibrary.ddd.core.DomainEvent

interface IntegrationEventMapper<out T : DomainEvent<*>> {
    fun toIntegrationEvent(event: @UnsafeVariance T): IntegrationEvent?
}