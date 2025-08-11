package pl.szczygieldev.shipmentsdk.model

import java.net.URL

enum class DeliveryProvider(
    val providerName: String,
    val displayName: String,
    val logoUrl: URL
) {
    //fixme: remove hardcoded url
    MockDeliveryProvider(
        "MockDeliveryProvider", "Mock Delivery Provider", URL("http://localhost:8080/images/shipping/mdp_logo.svg")
    )
}