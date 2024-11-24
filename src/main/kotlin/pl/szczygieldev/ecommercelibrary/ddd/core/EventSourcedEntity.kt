package pl.szczygieldev.ecommercelibrary.ddd.core

abstract class EventSourcedEntity<E : DomainEvent<E>> : Entity<E>() {
    private val events = mutableListOf<E>()
    fun occurredEvents(): List<E> = events.toList()
    fun clearOccurredEvents() = events.clear()

    var version: Int = 0

    protected abstract fun applyEvent(event: E)

    protected fun raiseEvent(event: E){
        events.add(event)
        applyEvent(event)
    }

    protected fun applyAll(events: List<E>) {
        events.forEach { event ->
            applyEvent(event)
            version++
        }
    }
}