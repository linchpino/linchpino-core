package com.linchpino.core.controller

import com.linchpino.core.security.JWTService
import com.linchpino.core.dto.TokenResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthenticationController(private val jwtService: JWTService) {

    @PostMapping("/login")
    fun login(authentication: Authentication): ResponseEntity<TokenResponse> {
        return ResponseEntity.ok(jwtService.token(authentication))
    }
}
