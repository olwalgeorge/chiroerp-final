package com.chiroerp.finance.tax.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
class TaxApplication : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("TaxApplication started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(TaxApplication::class.java, *args)
}
