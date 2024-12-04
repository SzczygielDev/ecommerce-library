package pl.szczygieldev.ecommercelibrary.command

import arrow.core.Either
import com.trendyol.kediatr.PipelineBehavior
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ExposedTxPipelineBehaviour : PipelineBehavior {
    override suspend fun <TRequest, TResponse> handle(
        request: TRequest,
        next: suspend (TRequest) -> TResponse
    ): TResponse {
        val result = newSuspendedTransaction {
            val response = next(request)
            if (response is Either<*, *>) {
                response.fold({
                    rollback()
                },{
                    commit()
                })

            }

            return@newSuspendedTransaction response
        }
        return result
    }
}