package com.chiroerp.shared.messaging.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

import org.springframework.boot.runApplication

@QuarkusMain
class MessagingApplication : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("MessagingApplication started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(MessagingApplication::class.java, *args)
}
