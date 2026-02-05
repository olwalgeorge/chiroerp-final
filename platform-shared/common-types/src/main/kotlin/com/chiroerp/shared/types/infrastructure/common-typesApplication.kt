package com.chiroerp.shared.types.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
class TypesApplication : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("TypesApplication started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(TypesApplication::class.java, *args)
}
