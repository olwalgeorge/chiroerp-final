package com.chiroerp.finance.gl.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
class FinanceGLApplication : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("FinanceGLApplication started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(FinanceGLApplication::class.java, *args)
}