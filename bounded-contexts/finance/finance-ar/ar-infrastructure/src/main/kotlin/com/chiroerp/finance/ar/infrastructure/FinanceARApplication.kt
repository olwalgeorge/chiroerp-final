package com.chiroerp.finance.ar.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
class FinanceARApplication : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("FinanceARApplication started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(FinanceARApplication::class.java, *args)
}