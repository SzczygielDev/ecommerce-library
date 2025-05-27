import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.kotest.core.spec.style.FunSpec
import pl.szczygieldev.ecommercelibrary.ddd.core.DomainEvent
import pl.szczygieldev.ecommercelibrary.ddd.core.UuidIdentity
import pl.szczygieldev.ecommercelibrary.eventstore.EventStore
import pl.szczygieldev.ecommercelibrary.eventstore.InMemoryEventStore
import pl.szczygieldev.ecommercelibrary.messaging.InMemoryMessageQueue
import pl.szczygieldev.ecommercelibrary.messaging.config.MessageQueueConfig
import java.util.UUID
import io.kotest.core.spec.IsolationMode
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.sql.Database
import pl.szczygieldev.ecommercelibrary.event.PollingAsyncEventHandler
import pl.szczygieldev.ecommercelibrary.event.ReactiveAsyncEventHandler

class EventHandlerTests : FunSpec() {

    class SomePollingAsyncEventHandler(override val eventStore: EventStore) :
        PollingAsyncEventHandler<SomeEvent>(
            SomeEvent::class,
            ObjectMapper(), eventStore, InMemoryMessageQueue(MessageQueueConfig())
        ) {
        override suspend fun handle(notification: SomeEvent) {
            //Do nothing
        }
    }

    class SomeFailingPollingAsyncEventHandler(override val eventStore: EventStore) :
        PollingAsyncEventHandler<SomeEvent>(
            SomeEvent::class,
            ObjectMapper(), eventStore, InMemoryMessageQueue(MessageQueueConfig())
        ) {
        override suspend fun handle(notification: SomeEvent) {
            throw Exception("Something went wrong")
        }
    }

    class SomeReactiveAsyncEventHandler(eventStore: EventStore) :
        ReactiveAsyncEventHandler<SomeEvent>(
            SomeEvent::class,
            ObjectMapper(), eventStore, InMemoryMessageQueue(MessageQueueConfig())
        ) {
        override suspend fun handle(notification: SomeEvent) {
            //Do nothing
        }
    }

    class SomeFailingReactiveAsyncEventHandler(eventStore: EventStore) :
        ReactiveAsyncEventHandler<SomeEvent>(
            SomeEvent::class,
            ObjectMapper(), eventStore, InMemoryMessageQueue(MessageQueueConfig())
        ) {
        override suspend fun handle(notification: SomeEvent) {
            throw Exception("Something went wrong")
        }
    }


    data class SomeIdentity(val id: UUID) : UuidIdentity<SomeIdentity>(id)
    class SomeEvent : DomainEvent<SomeEvent>()
    data class AnotherIdentity(val id: UUID) : UuidIdentity<SomeIdentity>(id)
    class AnotherEvent : DomainEvent<SomeEvent>()

    init {
        val db = Database.connect("jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1;", "org.h2.Driver")

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


        test("PollingAsyncEventHandler should process all provided events when no error occurred") {
            val handler = SomePollingAsyncEventHandler(eventStore)

            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)

            handler.processInternal()

            handler.eventQueue.isEmpty() shouldBe true
            handler.eventQueue.isDlqEmpty() shouldBe true
        }

        test("PollingAsyncEventHandler should put events on DLQ when error occurred") {
            val handler = SomeFailingPollingAsyncEventHandler(eventStore)

            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)

            handler.processInternal()

            handler.eventQueue.isEmpty() shouldBe true
            handler.eventQueue.isDlqEmpty() shouldBe false
        }

        test("ReactiveAsyncEventHandler should process all provided events when no error occurred") {
            val handler = SomeReactiveAsyncEventHandler(eventStore)

            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)

            handler.eventQueue.isEmpty() shouldBe true
            handler.eventQueue.isDlqEmpty() shouldBe true
        }

        test("ReactiveAsyncEventHandler should put events on DLQ when error occurred"){
            val handler = SomeFailingReactiveAsyncEventHandler(eventStore)

            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)
            eventStore.appendEvents(SomeIdentity(UUID.randomUUID()), listOf(SomeEvent()), 0)

            handler.eventQueue.isEmpty() shouldBe true
            handler.eventQueue.isDlqEmpty() shouldBe false
        }


    }
}