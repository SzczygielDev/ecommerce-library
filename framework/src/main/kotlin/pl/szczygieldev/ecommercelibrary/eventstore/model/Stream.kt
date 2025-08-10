package pl.szczygieldev.ecommercelibrary.eventstore.model

import pl.szczygieldev.ecommercelibrary.ddd.core.Identity


class Stream private constructor(val aggregateId: Identity<*>) {
    companion object {
        fun of(aggregateId: Identity<*>, events: List<StreamEntry>): Stream {
            val stream = Stream(aggregateId)
            stream.events.addAll(events)
            return stream
        }
    }

    private val events = mutableSetOf<StreamEntry>()
    fun getSortedEvents(): List<StreamEntry> = events.sortedBy { event -> event.version }.toList()
}