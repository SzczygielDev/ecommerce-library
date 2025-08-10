package pl.szczygieldev.ecommercelibrary.command
import java.time.Duration
import java.time.Instant

class CommandResult(
    val id: CommandId,
    var command: Command<*>,
    var status: CommandResultStatus,
    var timestamp: Instant,
    var duration: Duration,
    var errors: MutableList<CommandResultError>
)