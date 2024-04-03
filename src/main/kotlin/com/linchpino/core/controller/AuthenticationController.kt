package com.linchpino.core.controller

import com.linchpino.core.security.JWTService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthenticationController(private val jwtService: JWTService) {

    @PostMapping("/login")
    fun login(authentication: Authentication): JWTService.TokenResponse {
        return jwtService.token(authentication)
    }
}
