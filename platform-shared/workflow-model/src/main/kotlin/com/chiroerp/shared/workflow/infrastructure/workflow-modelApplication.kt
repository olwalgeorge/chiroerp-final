package com.chiroerp.shared.workflow.infrastructure

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

import org.springframework.boot.runApplication

@QuarkusMain
class WorkflowApplication : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        println("WorkflowApplication started with Quarkus")
        io.quarkus.runtime.Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    io.quarkus.runtime.Quarkus.run(WorkflowApplication::class.java, *args)
}
