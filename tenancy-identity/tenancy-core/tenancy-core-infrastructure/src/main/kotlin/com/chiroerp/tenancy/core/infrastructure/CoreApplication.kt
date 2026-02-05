package com.chiroerp.tenancy.core.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
class CoreApplication : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("CoreApplication started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(CoreApplication::class.java, *args)
}