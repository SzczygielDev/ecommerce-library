package pl.szczygieldev.ecommercelibrary.ddd.core

import java.lang.annotation.Inherited

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Inherited
annotation class AggregateRoot
