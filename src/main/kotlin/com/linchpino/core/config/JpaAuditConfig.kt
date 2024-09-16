package com.linchpino.core.config

import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.security.email
import java.util.Optional
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.context.SecurityContextHolder

@Configuration
@EnableJpaAuditing
class JpaAuditConfig {

    @Bean
    fun auditorProvider(accountRepository: AccountRepository): AuditorAware<Long> = AuditorAware<Long> {
        val auth = SecurityContextHolder.getContext().authentication ?: return@AuditorAware Optional.empty()
        val userId = accountRepository.findByEmailIgnoreCase(auth.email())?.id
        Optional.ofNullable(userId)
    }
}
