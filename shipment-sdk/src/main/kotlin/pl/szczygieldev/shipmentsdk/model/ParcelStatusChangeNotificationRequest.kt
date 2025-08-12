package pl.szczygieldev.shipmentsdk.model

import java.util.UUID

data class ParcelStatusChangeNotificationRequest(val parcelId: UUID, val parcelStatus: ParcelStatus)