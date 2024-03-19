package com.linchpino.core.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

	@Bean
	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
		return http
			.csrf { it.disable() }
			.cors { it.disable() }
			.authorizeHttpRequests { it.anyRequest().permitAll() }
			.build()
	}

	@Bean
	fun passwordEncoder():PasswordEncoder = BCryptPasswordEncoder()

}
