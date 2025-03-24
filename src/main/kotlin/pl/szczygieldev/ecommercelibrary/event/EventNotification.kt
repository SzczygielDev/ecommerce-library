package pl.szczygieldev.ecommercelibrary.event

import com.trendyol.kediatr.Notification
import pl.szczygieldev.ecommercelibrary.ddd.core.DomainEvent

class EventNotification<T : DomainEvent<T>> : Notification