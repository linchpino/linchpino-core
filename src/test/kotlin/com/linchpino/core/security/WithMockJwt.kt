package com.linchpino.core.security

import com.linchpino.core.enums.AccountTypeEnum
import java.time.Instant
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.test.context.TestSecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockJwtSecurityContextFactory::class)
annotation class WithMockJwt(val username: String = "user", val roles: Array<AccountTypeEnum> = [AccountTypeEnum.GUEST]) {
    companion object {
        fun mockAuthentication(email:String? = null):Authentication  = JwtAuthenticationToken(
            Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                mapOf("alg" to "none"),
                mapOf(
                    "sub" to (email?:"fake@example.com"),
                    "scope" to listOf<String>()
                )
            ), listOf()
        )
    }
}

class WithMockJwtSecurityContextFactory : WithSecurityContextFactory<WithMockJwt> {
    override fun createSecurityContext(withMockJwt: WithMockJwt): SecurityContext {
        val authorities = withMockJwt.roles.map { SimpleGrantedAuthority("SCOPE_${it.name}") }
        val jwtClaims = mapOf(
            "sub" to withMockJwt.username,
            "scope" to authorities.joinToString(" ") { it.authority }
        )

        val jwt = Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            mapOf("alg" to "none"),
            jwtClaims
        )

        val authToken = JwtAuthenticationToken(jwt, authorities)

        val context = TestSecurityContextHolder.getContext()
        context.authentication = authToken

        return context
    }
}
