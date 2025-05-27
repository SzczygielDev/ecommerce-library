package pl.szczygieldev.ecommercelibrary.event

import com.trendyol.kediatr.NotificationHandler
import pl.szczygieldev.ecommercelibrary.ddd.core.DomainEvent

abstract class EventHandler<T : DomainEvent<T>> : NotificationHandler<T> {
}