package com.linchpino.core.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.web.client.RestClient
import org.springframework.web.filter.OncePerRequestFilter

data class LinkedInUserInfoResponse(val email: String, val name: String)

class LinkedInSecurityFilter(
    private val restClient: RestClient,
    private val userInfoUri: String,
    private val userService: UserService
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
            val token = authentication.token.tokenValue
            val r = restClient.get()
                .uri(userInfoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .retrieve()
                .body(LinkedInUserInfoResponse::class.java) ?: throw RuntimeException("failed to fetch user info")

            try {
                val user = userService.loadUserByUsername(r.email) as SecurityUser
                val principal = OAuth2IntrospectionAuthenticatedPrincipal(user.username,
                    (authentication.principal as OAuth2IntrospectionAuthenticatedPrincipal).attributes,
                    user.authorities.map {
                        SimpleGrantedAuthority(
                            "SCOPE_${it.authority}"
                        )
                    })
                SecurityContextHolder.clearContext()
                val context = SecurityContextHolder.createEmptyContext()
                context.authentication =
                    BearerTokenAuthentication(principal, authentication.token, principal.authorities)
                SecurityContextHolder.getContextHolderStrategy().context = context
                securityContextRepository.saveContext(context, request, response)
            } catch (ex: UsernameNotFoundException) {
                logger.error(ex.message, ex)
            }
        }
        filterChain.doFilter(request, response)
    }
}
