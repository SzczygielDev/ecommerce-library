import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import pl.szczygieldev.ecommercelibrary.ddd.core.DomainEvent
import pl.szczygieldev.ecommercelibrary.ddd.core.UuidIdentity
import pl.szczygieldev.ecommercelibrary.eventstore.EventStore
import pl.szczygieldev.ecommercelibrary.eventstore.InMemoryEventStore
import java.util.*

class EventStoreTests : FunSpec() {
    data class SomeIdentity(val id: UUID) : UuidIdentity<SomeIdentity>(id)
    class SomeEvent : DomainEvent<SomeEvent>()
    data class AnotherIdentity(val id: UUID) : UuidIdentity<SomeIdentity>(id)
    class AnotherEvent : DomainEvent<SomeEvent>()

    init {
        isolationMode = IsolationMode.InstancePerLeaf

        val kotlinModule = KotlinModule.Builder()
            .enable(KotlinFeature.SingletonSupport)
            .build()

        val objectMapper = JsonMapper.builder()
            .addModule(kotlinModule)
            .addModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build()

        val eventStore: EventStore = InMemoryEventStore(objectMapper)

        test("Events should be returned by provided type") {
            // Arrange
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)

            // Act
            val eventsForType = eventStore.getEventsForType<SomeEvent>(SomeEvent::class.java.typeName, 0, 10)

            // Assert
            eventsForType.size.shouldBe(2)
        }

        test("Events should be returned by provided type when other events are present") {
            // Arrange
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)
            eventStore.appendEvents(AnotherIdentity(UUID.randomUUID()), listOf(AnotherEvent()), 0)
            eventStore.appendEvents(AnotherIdentity(UUID.randomUUID()), listOf(AnotherEvent()), 0)


            // Act
            val eventsForType = eventStore.getEventsForType<SomeEvent>(SomeEvent::class.java.typeName, 0, 10)

            // Assert
            eventsForType.size.shouldBe(2)
        }

        test("Events should be returned by provided type when other events are present in mixed order") {
            // Arrange
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)
            eventStore.appendEvents(AnotherIdentity(UUID.randomUUID()), listOf(AnotherEvent()), 0)
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)
            eventStore.appendEvents(AnotherIdentity(UUID.randomUUID()), listOf(AnotherEvent()), 0)
            eventStore.appendEvents(AnotherIdentity(UUID.randomUUID()), listOf(AnotherEvent()), 0)


            // Act
            val eventsForType = eventStore.getEventsForType<SomeEvent>(SomeEvent::class.java.typeName, 0, 10)

            // Assert
            eventsForType.size.shouldBe(2)
        }

        test("Events should be returned with provided limit") {
            // Arrange
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)

            // Act
            val eventsForType = eventStore.getEventsForType<SomeEvent>(SomeEvent::class.java.typeName, 0, 2)

            // Assert
            eventsForType.size.shouldBe(2)
        }

        test("Events should be returned with provided offset") {
            // Arrange
            val lastEvent = SomeEvent()
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(lastEvent), 0)


            // Act
            val eventsForType = eventStore.getEventsForType<SomeEvent>(SomeEvent::class.java.typeName, 2, 10)

            // Assert
            eventsForType.first().sameEventAs(lastEvent)
            eventsForType.size.shouldBe(1)
        }
    }
}