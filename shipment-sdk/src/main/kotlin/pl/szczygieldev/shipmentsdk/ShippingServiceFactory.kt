package pl.szczygieldev.shipmentsdk

import pl.szczygieldev.shipmentsdk.impl.FakeShippingService

class ShippingServiceFactory {
    companion object{
        fun create(): ShippingService {
            return FakeShippingService()
        }
    }
}