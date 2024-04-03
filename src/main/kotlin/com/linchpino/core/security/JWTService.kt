package com.linchpino.core.security

import com.linchpino.core.dto.TokenResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class JWTService(private val jwtEncoder: JwtEncoder) {

    fun token(authentication: Authentication): TokenResponse {
        val now = Instant.now()
        val scope = authentication.authorities.joinToString(separator = " ") { it.authority }
        val claims = JwtClaimsSet.builder()
            .issuer("https://linchpino.com")
            .issuedAt(now)
            .expiresAt(now.plus(60, ChronoUnit.MINUTES))
            .subject(authentication.name)
            .claim("scope", scope)
            .build()
        val token = this.jwtEncoder.encode(JwtEncoderParameters.from(claims))

        return TokenResponse(token.tokenValue, token.expiresAt)
    }
}
