package pl.szczygieldev.ecommercelibrary.ddd.core

import com.trendyol.kediatr.Notification
import java.time.Instant
import java.util.UUID

abstract class DomainEvent<T : DomainEvent<T>> : Notification {
    val id: String = UUID.randomUUID().toString()

    val occurredOn: Instant = Instant.now()

    fun sameEventAs(other: DomainEvent<*>): Boolean = id == other.id
}