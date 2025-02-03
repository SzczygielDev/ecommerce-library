package pl.szczygieldev.ecommercelibrary.eventstore.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

internal object ExposedEventStoreTable : Table("exposed_event_store_events") {
    val id = integer("id").uniqueIndex().autoIncrement()
    val aggregateId = varchar("aggregateId", 255)
    val version = integer("version")
    val eventType = text("eventType")
    val eventData = text("eventData")
    val timestamp = timestamp("timestamp")
}