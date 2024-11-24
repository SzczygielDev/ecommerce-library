package pl.szczygieldev.ecommercelibrary.eventstore.exception

import pl.szczygieldev.ecommercelibrary.ddd.core.Identity

class EventStoreLockingException(val aggregateId: Identity<*>, val currentVersion: Int?, val exceptedVersion: Int) : RuntimeException("Optimistic locking exception for aggregate='${aggregateId.id()}' version='$currentVersion exceptedVersion=$exceptedVersion'")