package com.chiroerp.finance.ap.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
class FinanceAPApplication : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("FinanceAPApplication started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(FinanceAPApplication::class.java, *args)
}