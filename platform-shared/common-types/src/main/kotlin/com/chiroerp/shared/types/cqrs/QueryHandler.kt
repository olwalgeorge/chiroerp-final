package com.chiroerp.shared.types.cqrs

import com.chiroerp.shared.types.results.Result

fun interface QueryHandler<in Q : Query<R>, out R> {
    fun handle(query: Q): Result<R>
}
