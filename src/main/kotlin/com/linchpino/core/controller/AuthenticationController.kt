package com.linchpino.core.controller

import com.linchpino.core.dto.TokenResponse
import com.linchpino.core.security.JWTService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthenticationController(private val jwtService: JWTService) {

    @Operation(summary = "Login with basic authentication")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Provide username and password with basic authentication"
            ),
            ApiResponse(responseCode = "401", description = "Invalid username or password")
        ]
    )
    @PostMapping("/login")
    fun login(authentication: Authentication): ResponseEntity<TokenResponse> {
        return ResponseEntity.ok(jwtService.token(authentication))
    }
}
