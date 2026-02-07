package com.chiroerp.shared.types.aggregate

abstract class Entity<ID : Any>(open val id: ID) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as Entity<*>
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
