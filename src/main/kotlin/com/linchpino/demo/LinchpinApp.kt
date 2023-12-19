package com.linchpino.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class LinchpinApp

fun main(args: Array<String>) {
    runApplication<LinchpinApp>(*args)
}