package com.linchpino.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController("api")
@SpringBootApplication
class LinchpinApp

fun main(args: Array<String>) {
    runApplication<LinchpinApp>(*args)
}

@GetMapping("/test")
fun helloKotlin(): String {
    return "hello world"
}