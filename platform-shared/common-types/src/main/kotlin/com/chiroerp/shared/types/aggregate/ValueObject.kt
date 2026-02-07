package com.chiroerp.shared.types.aggregate

abstract class ValueObject {
    protected abstract fun atomicValues(): List<Any?>

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ValueObject
        return atomicValues() == other.atomicValues()
    }

    override fun hashCode(): Int = atomicValues().hashCode()
}
