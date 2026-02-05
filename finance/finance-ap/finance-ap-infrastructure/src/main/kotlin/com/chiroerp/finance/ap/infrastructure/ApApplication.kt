package com.chiroerp.finance.ap.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
class ApApplication : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("ApApplication started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(ApApplication::class.java, *args)
}
