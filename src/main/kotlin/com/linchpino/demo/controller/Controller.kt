package com.linchpino.demo.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class Controller {
    @GetMapping("/test")
    fun getHelloWorld(): String {
        return "Hello world!!!"
    }
}