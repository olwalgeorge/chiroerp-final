package com.chiroerp.shared.security.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

import org.springframework.boot.runApplication

@QuarkusMain
class SecurityApplication : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("SecurityApplication started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(SecurityApplication::class.java, *args)
}
