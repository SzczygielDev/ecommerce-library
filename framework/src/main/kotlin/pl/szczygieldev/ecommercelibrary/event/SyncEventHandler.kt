package pl.szczygieldev.ecommercelibrary.event

import pl.szczygieldev.ecommercelibrary.ddd.core.DomainEvent

abstract class SyncEventHandler<T : DomainEvent<T>> : EventHandler<T>()