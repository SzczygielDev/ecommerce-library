package pl.szczygieldev.ecommercelibrary.ddd.core

import java.util.*

abstract class UuidIdentity<T : UuidIdentity<T>>(private val id: UUID) : Identity<T>(id.toString()) {
    fun idAsUUID(): UUID = id
}