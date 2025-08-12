package pl.szczygieldev.shipmentsdk.impl

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import pl.szczygieldev.shipmentsdk.ShippingService
import pl.szczygieldev.shipmentsdk.impl.model.ParcelLabelResponse
import pl.szczygieldev.shipmentsdk.impl.model.RegisterParcelRequest
import pl.szczygieldev.shipmentsdk.impl.model.RegisterParcelResponse
import pl.szczygieldev.shipmentsdk.model.*
import java.net.URL
import java.util.*

internal class FakeShippingService(apiKey: String) : ShippingService {
    private val webClient = WebClient.builder()
        .baseUrl("http://localhost:8080/external/shipping/")
        .defaultHeader("X-API-KEY", apiKey)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()

    override fun registerParcel(parcelDimensions: ParcelDimensions, deliveryProvider: DeliveryProvider): ParcelId? {
        return when (deliveryProvider) {
            DeliveryProvider.MockDeliveryProvider -> {
                val response = webClient.post()
                    .uri("/register")
                    .bodyValue(
                        RegisterParcelRequest(
                            parcelDimensions.width,
                            parcelDimensions.length,
                            parcelDimensions.height,
                            parcelDimensions.weight
                        )
                    )
                    .exchangeToMono<RegisterParcelResponse?> { response ->

                        if (!response.statusCode().is2xxSuccessful) {
                            return@exchangeToMono null
                        }

                        return@exchangeToMono response.bodyToMono<RegisterParcelResponse>()
                    }
                    .onErrorComplete()
                    .block() ?: return null

                ParcelId(UUID.fromString(response.parcelId))
            }


        }
    }

    override fun getLabel(parcelId: ParcelId): ParcelLabel? {
        val response = webClient.get()
            .uri("/label")
            .accept(MediaType.APPLICATION_JSON)
            .exchangeToMono<ParcelLabelResponse?> { response ->

                if (!response.statusCode().is2xxSuccessful) {
                    return@exchangeToMono null
                }

                return@exchangeToMono response.bodyToMono<ParcelLabelResponse>()
            }
            .onErrorComplete()
            .block() ?: return null

        return ParcelLabel(URL(response.url))
    }
}