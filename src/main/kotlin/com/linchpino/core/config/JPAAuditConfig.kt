package com.linchpino.core.config

import com.linchpino.core.repository.AccountRepository
import java.util.Optional
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal

@Configuration
class JPAAuditConfig {

    @Bean
    fun auditorProvider(accountRepository: AccountRepository): AuditorAware<Long> {
        return AuditorAware {
            SecurityContextHolder.getContext().authentication?.let { authentication ->
                val email = when (authentication) {
                    is JwtAuthenticationToken -> authentication.name
                    is BearerTokenAuthentication -> (authentication.principal as OAuth2IntrospectionAuthenticatedPrincipal).name
                    else -> null
                } ?: return@AuditorAware Optional.empty()

                val id = accountRepository.findByEmailIgnoreCase(email)?.id
                return@AuditorAware Optional.ofNullable<Long>(id)

            } ?: return@AuditorAware Optional.empty()

        }
    }
}
