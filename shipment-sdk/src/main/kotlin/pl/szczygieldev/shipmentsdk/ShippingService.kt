package pl.szczygieldev.shipmentsdk

import pl.szczygieldev.shipmentsdk.model.*

interface ShippingService {
    fun registerParcel(parcelDimensions: ParcelDimensions, deliveryProvider: DeliveryProvider): ParcelId?

    fun getLabel(parcelId: ParcelId): ParcelLabel?
}