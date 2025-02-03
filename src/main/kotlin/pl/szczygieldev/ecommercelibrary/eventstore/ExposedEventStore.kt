package pl.szczygieldev.ecommercelibrary.eventstore

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import pl.szczygieldev.ecommercelibrary.ddd.core.DomainEvent
import pl.szczygieldev.ecommercelibrary.ddd.core.Identity
import pl.szczygieldev.ecommercelibrary.eventstore.exception.EventStoreLockingException
import pl.szczygieldev.ecommercelibrary.eventstore.model.Stream
import pl.szczygieldev.ecommercelibrary.eventstore.model.StreamEntry
import pl.szczygieldev.ecommercelibrary.eventstore.table.ExposedEventStoreTable
import javax.sql.DataSource

class ExposedEventStore(dataSource: DataSource, val objectMapper: ObjectMapper) : EventStore {

    init {
        transaction(Database.connect(dataSource)) {
            SchemaUtils.create(ExposedEventStoreTable)
        }
    }

    private fun findStreamByAggregateId(aggregateId: Identity<*>): Stream? {
        val foundStreamEntries =
            ExposedEventStoreTable.selectAll().where(ExposedEventStoreTable.aggregateId.eq(aggregateId.id()))
                .map { row ->
                    StreamEntry(
                        aggregateId,
                        row[ExposedEventStoreTable.version].toInt(),
                        row[ExposedEventStoreTable.eventType],
                        row[ExposedEventStoreTable.eventData]
                    )
                }.toList()
        if (foundStreamEntries.isEmpty()) {
            return null
        }

        return Stream.of(aggregateId, foundStreamEntries)
    }


    override fun appendEvents(aggregateId: Identity<*>, events: List<DomainEvent<*>>, exceptedVersion: Int): Unit =
        transaction {
            val foundStream = findStreamByAggregateId(aggregateId)
            val eventsForAggregate = foundStream?.getSortedEvents()?.toMutableList()

            var currentVersion = eventsForAggregate?.lastOrNull()?.version

            if (eventsForAggregate == null) {
                currentVersion = 0
            } else if (currentVersion != exceptedVersion) {
                throw EventStoreLockingException(aggregateId, currentVersion, exceptedVersion)
            }

            var versionOfCurrentEvent = currentVersion
            ExposedEventStoreTable.batchInsert(events, shouldReturnGeneratedValues = false) { event ->
                versionOfCurrentEvent++
                this[ExposedEventStoreTable.aggregateId] = aggregateId.id()
                this[ExposedEventStoreTable.version] = versionOfCurrentEvent
                this[ExposedEventStoreTable.eventType] = event.javaClass.typeName
                this[ExposedEventStoreTable.eventData] = objectMapper.writeValueAsString(event)
                this[ExposedEventStoreTable.timestamp] = event.occurredOn.toKotlinInstant()
            }
        }

    override fun <T : DomainEvent<T>> getEvents(aggregateId: Identity<*>): List<T>? {
        return findStreamByAggregateId(aggregateId)?.getSortedEvents()?.map {
            return@map objectMapper.readerFor(Class.forName(it.eventType)).readValue<T>(it.eventData)
        }
    }
}