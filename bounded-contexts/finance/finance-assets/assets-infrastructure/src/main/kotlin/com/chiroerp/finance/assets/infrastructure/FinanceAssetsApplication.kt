package com.chiroerp.finance.assets.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
class FinanceAssetsApplication : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("FinanceAssetsApplication started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(FinanceAssetsApplication::class.java, *args)
}