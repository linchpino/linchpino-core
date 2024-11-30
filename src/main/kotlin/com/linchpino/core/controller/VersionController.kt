package com.linchpino.core.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/version")
class VersionController(@Value("\${build.version}") private val version: String) {

    @GetMapping
    fun get():VersionResponse{
        return VersionResponse(version)
    }
    data class VersionResponse(val version: String)
}
