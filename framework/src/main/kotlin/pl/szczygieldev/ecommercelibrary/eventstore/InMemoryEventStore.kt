package pl.szczygieldev.ecommercelibrary.eventstore

import com.fasterxml.jackson.databind.ObjectMapper
import pl.szczygieldev.ecommercelibrary.ddd.core.DomainEvent
import pl.szczygieldev.ecommercelibrary.ddd.core.Identity
import pl.szczygieldev.ecommercelibrary.event.ReactiveEventHandler
import pl.szczygieldev.ecommercelibrary.eventstore.exception.EventStoreLockingException
import pl.szczygieldev.ecommercelibrary.eventstore.model.Stream
import pl.szczygieldev.ecommercelibrary.eventstore.model.StreamEntry
import kotlin.reflect.jvm.jvmName
import kotlinx.coroutines.*

class InMemoryEventStore(val objectMapper: ObjectMapper) : EventStore {
    private val store = mutableMapOf<String, Stream>()

    // Event class name : list of listeners
    private val listeners = mutableMapOf<String, MutableList<ReactiveEventHandler<*>>>()

    override suspend fun appendEvents(aggregateId: Identity<*>, events: List<DomainEvent<*>>, exceptedVersion: Int) {
        val foundStream = store[aggregateId.id()]

        var eventsForAggregate = foundStream?.getSortedEvents()?.toMutableList()

        var currentVersion = eventsForAggregate?.lastOrNull()?.version

        if (eventsForAggregate == null) {
            currentVersion = 0
            eventsForAggregate = mutableListOf()
        } else if (currentVersion != exceptedVersion) {
            throw EventStoreLockingException(aggregateId, currentVersion, exceptedVersion)
        }

        var versionOfCurrentEvent = currentVersion
        events.forEach { event ->
            versionOfCurrentEvent++

            eventsForAggregate.add(
                StreamEntry(
                    aggregateId,
                    versionOfCurrentEvent,
                    event.javaClass.typeName,
                    objectMapper.writeValueAsString(event)
                )
            )
        }

        store[aggregateId.id()] = Stream.of(aggregateId, eventsForAggregate)

        for (event in events) {
            coroutineScope {
                listeners[event::class.jvmName]?.let {
                    it.forEach { listener -> launch { listener.notify() } }
                }
            }
        }
    }

    final override fun <T : DomainEvent<T>> getEvents(aggregateId: Identity<*>): List<T>? {
        return store[aggregateId.id()]?.getSortedEvents()?.map {
            return@map objectMapper.readerFor(Class.forName(it.eventType)).readValue<T>(it.eventData)
        }
    }

    override fun <T : DomainEvent<T>> getEventsForType(type: String, offset: Int, limit: Int): List<T> {
        return store.values.map { stream ->
            stream.getSortedEvents().filter { it.eventType == type }.map { event ->
                objectMapper.readerFor(Class.forName(event.eventType)).readValue<T>(event.eventData)
            }
        }.flatten().drop(offset).take(limit)
    }

    override fun <T : DomainEvent<T>> registerListener(type: String, listener: ReactiveEventHandler<T>) {
        val listenersForType = listeners[type]
        if (listenersForType == null) {
            listeners[type] = mutableListOf(listener)
        } else {
            listenersForType.add(listener)
        }
    }
}