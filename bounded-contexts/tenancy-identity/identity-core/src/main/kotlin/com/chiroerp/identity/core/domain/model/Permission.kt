package com.chiroerp.identity.core.domain.model

import java.util.Locale

data class Permission(
    val objectId: String,
    val actions: Set<String>,
    val constraints: Map<String, String> = emptyMap(),
) {
    init {
        require(objectId.isNotBlank()) { "Authorization object cannot be blank" }
        require(actions.isNotEmpty()) { "Permission must grant at least one action" }
    }

    private val normalizedObjectId: String = objectId.trim().uppercase(Locale.ROOT)
    private val normalizedActions: Set<String> = actions.map { it.trim().uppercase(Locale.ROOT) }.toSet()

    fun allows(action: String, context: Map<String, String> = emptyMap()): Boolean {
        val normalizedAction = action.trim().uppercase(Locale.ROOT)
        if (normalizedAction !in normalizedActions) {
            return false
        }
        return constraints.all { (key, requiredValue) ->
            val candidate = context[key]
            requiredValue.isBlank() || candidate == requiredValue
        }
    }

    fun matchesObject(targetObjectId: String): Boolean =
        normalizedObjectId == targetObjectId.trim().uppercase(Locale.ROOT)
}
