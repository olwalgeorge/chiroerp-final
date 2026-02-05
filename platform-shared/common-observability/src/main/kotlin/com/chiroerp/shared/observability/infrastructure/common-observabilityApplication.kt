package com.chiroerp.shared.observability.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

import org.springframework.boot.runApplication

@QuarkusMain
class ObservabilityApplication : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("ObservabilityApplication started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(ObservabilityApplication::class.java, *args)
}
