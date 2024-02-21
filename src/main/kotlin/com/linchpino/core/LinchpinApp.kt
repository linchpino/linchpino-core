package com.linchpino.core

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class LinchpinApp

fun main(args: Array<String>) {
    runApplication<LinchpinApp>(*args)
}