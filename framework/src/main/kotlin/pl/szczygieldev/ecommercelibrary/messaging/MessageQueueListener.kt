package pl.szczygieldev.ecommercelibrary.messaging

interface MessageQueueListener{
    suspend fun onMessageAvailable()
}