package com.chiroerp.shared.config.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

import org.springframework.boot.runApplication

@QuarkusMain
class ConfigApplication : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("ConfigApplication started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(ConfigApplication::class.java, *args)
}
