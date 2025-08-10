package pl.szczygieldev.ecommercelibrary.event

import pl.szczygieldev.ecommercelibrary.ddd.core.DomainEvent
import pl.szczygieldev.ecommercelibrary.messaging.*

abstract class AsyncEventHandler<T : DomainEvent<T>>(open val eventQueue: MessageQueue<T>) : EventHandler<T>()