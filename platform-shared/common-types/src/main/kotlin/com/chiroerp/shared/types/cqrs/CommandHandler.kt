package com.chiroerp.shared.types.cqrs

import com.chiroerp.shared.types.results.Result

fun interface CommandHandler<in C : Command, out R> {
    fun handle(command: C): Result<R>
}
