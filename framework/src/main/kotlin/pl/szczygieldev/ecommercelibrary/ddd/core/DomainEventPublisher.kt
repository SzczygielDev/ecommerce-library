package pl.szczygieldev.ecommercelibrary.ddd.core

interface DomainEventPublisher<T : DomainEvent<*>> {
    fun publish(domainEvent: T)

    fun publishBatch(events: List<T>)
}