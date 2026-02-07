package com.chiroerp.shared.types.primitives

/**
 * Generic identifier wrapper for type-safe IDs.
 */
data class Identifier<T : Any>(val value: T)
