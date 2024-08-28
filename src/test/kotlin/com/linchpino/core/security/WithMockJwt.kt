package com.linchpino.core.security

import com.linchpino.core.enums.AccountTypeEnum
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal
import org.springframework.security.test.context.TestSecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory
import java.time.Instant

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockJwtSecurityContextFactory::class)
annotation class WithMockJwt(
    val username: String = "user",
    val roles: Array<AccountTypeEnum> = [AccountTypeEnum.GUEST]
) {
    companion object {
        fun mockAuthentication(): Authentication = JwtAuthenticationToken(
            Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                mapOf("alg" to "none"),
                mapOf(
                    "sub" to "fake@example.com",
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


@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockBearerTokenSecurityContextFactory::class)
annotation class WithMockBearerToken(
    val username: String = "user",
    val roles: Array<AccountTypeEnum> = [AccountTypeEnum.GUEST]
)

class WithMockBearerTokenSecurityContextFactory : WithSecurityContextFactory<WithMockBearerToken> {

    override fun createSecurityContext(withMockBearerToken: WithMockBearerToken): SecurityContext {
        val authorities = withMockBearerToken.roles.map { SimpleGrantedAuthority("SCOPE_${it.name}") }
        val principal = OAuth2IntrospectionAuthenticatedPrincipal(
            withMockBearerToken.username,
            mapOf("key" to "value"),
            authorities
        )
        val authToken = BearerTokenAuthentication(
            principal, OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, "dummy token",
                Instant.now(), Instant.now().plusSeconds(240)
            ), authorities
        )

        val context = TestSecurityContextHolder.getContext()
        context.authentication = authToken

        return context
    }
}
