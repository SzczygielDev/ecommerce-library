import arrow.core.Either
import arrow.core.raise.either
import com.trendyol.kediatr.CommandWithResultHandler
import com.trendyol.kediatr.MappingDependencyProvider.Companion.createMediator
import com.trendyol.kediatr.Mediator
import io.kotest.common.runBlocking
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import pl.szczygieldev.ecommercelibrary.command.Command
import pl.szczygieldev.ecommercelibrary.command.CommandError
import pl.szczygieldev.ecommercelibrary.command.ExposedTxPipelineBehaviour

class ExposedTxPipelineBehaviourTests {
    private class SuccessCommand : Command<CommandError>()

    private class FailingCommand : Command<CommandError>()

    private class SuccessCommandHandler :
        CommandWithResultHandler<SuccessCommand, Either<CommandError, Unit>> {

        override suspend fun handle(command: SuccessCommand): Either<CommandError, Unit> = either {
            TxTestTable.insert {
                it[name] = "A"
            }

            TxTestTable.insert {
                it[name] = "B"
            }

            TxTestTable.insert {
                it[name] = "C"
            }
        }
    }

    private class FailingCommandHandler :
        CommandWithResultHandler<FailingCommand, Either<CommandError, Unit>> {

        override suspend fun handle(command: FailingCommand): Either<CommandError, Unit> = either {
            try {
                TxTestTable.insert {
                    it[name] = "A"
                }

                TxTestTable.insert {
                    it[name] = "A"
                }

                TxTestTable.insert {
                    it[name] = "B"
                }
            } catch (e: Exception) {
                raise(object : CommandError("Some error") {})
            }
        }
    }

    val mediator: Mediator = createMediator(
        handlers = listOf(
            SuccessCommandHandler(),
            FailingCommandHandler(),
            ExposedTxPipelineBehaviour()
        )
    )
    object TxTestTable : IntIdTable() {
        val name = varchar("someColumn", 50).uniqueIndex()
    }

    val db = Database.connect("jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1;", "org.h2.Driver")

    init {
        transaction {
            SchemaUtils.create(TxTestTable)
        }
    }

    @BeforeEach
    fun clearDb() {
        transaction {
            TxTestTable.deleteAll()
        }
    }

    @Test
    @DisplayName("Should transaction be committed when all operations succeed")
    fun should_transactionBeCommitted_when_allOperationsSucceed(): Unit = runBlocking {
        mediator.send(SuccessCommand())

        val entityCount = transaction {
            return@transaction TxTestTable.selectAll().count()
        }
        entityCount.shouldBe(3)
    }

    @Test
    @DisplayName("Should transaction be rolled back when any of operation fails")
    fun should_transactionBeRolledBack_when_anyOfOperationFails(): Unit = runBlocking {
        mediator.send(FailingCommand())

        val entityCount = transaction {
            return@transaction TxTestTable.selectAll().count()
        }

        entityCount.shouldBe(0)
    }
}