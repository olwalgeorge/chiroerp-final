package com.chiroerp.finance.tax.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
class FinanceTaxApplication : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("FinanceTaxApplication started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(FinanceTaxApplication::class.java, *args)
}