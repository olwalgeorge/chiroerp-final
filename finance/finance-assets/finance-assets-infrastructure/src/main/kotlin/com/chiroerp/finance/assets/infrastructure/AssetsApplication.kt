package com.chiroerp.finance.assets.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
class AssetsApplication : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("AssetsApplication started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(AssetsApplication::class.java, *args)
}
