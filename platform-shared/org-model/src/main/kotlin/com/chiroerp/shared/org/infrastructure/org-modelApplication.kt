package com.chiroerp.shared.org.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

import org.springframework.boot.runApplication

@QuarkusMain
class OrgApplication : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("OrgApplication started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(OrgApplication::class.java, *args)
}
