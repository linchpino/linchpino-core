package com.linchpino.core.config

import com.linchpino.core.repository.AccountRepository
import jakarta.persistence.EntityManagerFactory
import java.util.Optional
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal

@Configuration
@EnableJpaAuditing
class JPAAuditConfig(private val entityManagerFactory: EntityManagerFactory) {

    @Bean
    fun auditorProvider(accountRepository: AccountRepository): AuditorAware<Long> {
        return AuditorAware {
            SecurityContextHolder.getContext().authentication?.let { authentication ->
                val email = when (authentication) {
                    is JwtAuthenticationToken -> authentication.name
                    is BearerTokenAuthentication -> (authentication.principal as OAuth2IntrospectionAuthenticatedPrincipal).name
                    else -> null
                } ?: return@AuditorAware Optional.empty()

                val id = findUserIdByEmail(email)
                return@AuditorAware Optional.ofNullable<Long>(id)

            } ?: return@AuditorAware Optional.empty()

        }
    }

    fun findUserIdByEmail(email: String): Long? {
        val em = entityManagerFactory.createEntityManager()
        em.transaction.begin()
        val result = em.createQuery("select a.id from Account a where a.email = :email", Long::class.java)
            .setParameter("email", email).resultList
        em.transaction.commit()
        em.close()
        return if(result.isEmpty()) null else result[0]
    }
}

