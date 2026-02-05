package com.chiroerp.finance.ar.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

import org.springframework.boot.runApplication

@QuarkusMain
class ArApplication : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("ArApplication started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(ArApplication::class.java, *args)
}
