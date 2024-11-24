package pl.szczygieldev.ecommercelibrary.ddd.core

interface DomainEventHandler<T : DomainEvent<*>> {
    suspend fun handleEvent(domainEvent: T)
}