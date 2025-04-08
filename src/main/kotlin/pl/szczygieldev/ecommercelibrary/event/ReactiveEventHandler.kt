package pl.szczygieldev.ecommercelibrary.event

import pl.szczygieldev.ecommercelibrary.ddd.core.DomainEvent

interface ReactiveEventHandler<T : DomainEvent<T>> {
    suspend fun notify()
}