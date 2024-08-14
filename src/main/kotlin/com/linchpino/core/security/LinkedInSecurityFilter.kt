package com.linchpino.core.security

import com.linchpino.core.service.LinkedInService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.web.filter.OncePerRequestFilter


class LinkedInSecurityFilter(
    private val linkedInService: LinkedInService,
) : OncePerRequestFilter() {

    private val securityContextRepository: SecurityContextRepository = RequestAttributeSecurityContextRepository()

    public override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null) {
            filterChain.doFilter(request, response)
            return
        }
        if (authentication is BearerTokenAuthentication) {
            val userInfo = linkedInService.userInfo(authentication.token.tokenValue)
            val principal = OAuth2IntrospectionAuthenticatedPrincipal(
                userInfo.email,
                (authentication.principal as OAuth2IntrospectionAuthenticatedPrincipal).attributes,
                listOf(SimpleGrantedAuthority("SCOPE_JOB_SEEKER"))
            )
            SecurityContextHolder.clearContext()
            val context = SecurityContextHolder.createEmptyContext()
            context.authentication =
                BearerTokenAuthentication(principal, authentication.token, principal.authorities)
            SecurityContextHolder.getContextHolderStrategy().context = context
            securityContextRepository.saveContext(context, request, response)
        }
        filterChain.doFilter(request, response)
    }
}
