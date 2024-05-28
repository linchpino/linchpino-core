package com.linchpino.core.security

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal

fun Authentication.email(): String = when (this) {
    is JwtAuthenticationToken -> this.name
    is BearerTokenAuthentication -> (this.principal as OAuth2IntrospectionAuthenticatedPrincipal).name
    else -> throw UnsupportedOperationException("Authentication type not supported")
}
