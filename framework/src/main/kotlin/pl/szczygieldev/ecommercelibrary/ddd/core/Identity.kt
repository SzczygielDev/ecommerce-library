package pl.szczygieldev.ecommercelibrary.ddd.core
import java.io.Serializable

abstract class Identity<T : Identity<T>>(private val id: String) : Serializable {
    fun id(): String = id
    fun sameValueAs(other: T): Boolean = id == other.id
}