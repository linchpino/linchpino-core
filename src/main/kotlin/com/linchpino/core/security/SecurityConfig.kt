package com.linchpino.core.security

import com.linchpino.core.service.LinkedInService
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.security.authentication.AuthenticationManagerResolver
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.intercept.AuthorizationFilter
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.net.URI

@Configuration
@EnableMethodSecurity
class SecurityConfig(private val rsaKeys: RSAKeys) {

    @Bean
    fun securityFilterChain(http: HttpSecurity,
                            opaqueTokenIntrospector: OpaqueTokenIntrospector,
                            linkedInService: LinkedInService): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests {
                it.requestMatchers("/login").authenticated()
                it.requestMatchers("/api/accounts/profile").authenticated()
                it.requestMatchers("/api/accounts/search").hasAnyAuthority("SCOPE_ADMIN")
                it.requestMatchers("/api/admin/**").hasAnyAuthority("SCOPE_ADMIN")
                it.requestMatchers("/api/interviews/*/feedback").hasAnyAuthority("SCOPE_JOB_SEEKER")
                it.requestMatchers("/api/interviews/mentors/**").hasAnyAuthority("SCOPE_MENTOR")
                it.requestMatchers("/api/interviews/jobseekers/**").hasAnyAuthority("SCOPE_JOB_SEEKER")
                it.requestMatchers("/api/accounts/image").authenticated()
                it.anyRequest().permitAll()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .oauth2ResourceServer {
                it.authenticationManagerResolver(tokenAuthenticationManagerResolver(opaqueTokenIntrospector))
            }
            .addFilterBefore(LinkedInSecurityFilter(linkedInService),AuthorizationFilter::class.java)
            .httpBasic(Customizer.withDefaults())
            .build()
    }


    private fun tokenAuthenticationManagerResolver(opaqueTokenIntrospector: OpaqueTokenIntrospector): AuthenticationManagerResolver<HttpServletRequest> {

        return AuthenticationManagerResolver { request ->
            val bearerToken = request.getHeader("Authorization").replace("Bearer ", "")
            if (isJwtFormat(bearerToken)) {
                ProviderManager(JwtAuthenticationProvider(jwtDecoder()))
            } else {
                ProviderManager(OpaqueTokenAuthenticationProvider(opaqueTokenIntrospector))
            }
        }
    }

    private fun isJwtFormat(token: String): Boolean {
        return try {
            jwtDecoder().decode(token)
            true
        }catch (ex: JwtException){
            false
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun jwtDecoder(): NimbusJwtDecoder = NimbusJwtDecoder.withPublicKey(rsaKeys.publicKey).build()

    @Bean
    fun jwtEncoder(): NimbusJwtEncoder {
        val jwk = RSAKey.Builder(rsaKeys.publicKey).privateKey(rsaKeys.privateKey).build()
        return NimbusJwtEncoder(ImmutableJWKSet(JWKSet(jwk)))
    }

    @Bean
    fun opaqueTokenIntrospector(
        @Value("\${linkedin.clientId}") clientId:String,
        @Value("\${linkedin.secret}") clientSecret:String,
    ): OpaqueTokenIntrospector {
        val restTemplate = RestTemplate()
        val introspectUri = "https://www.linkedin.com/oauth/v2/introspectToken"
        val opaqueTokenIntrospector = SpringOpaqueTokenIntrospector(introspectUri, restTemplate)
        opaqueTokenIntrospector.setRequestEntityConverter { token: String? ->
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
            val body: MultiValueMap<String, String> = LinkedMultiValueMap()
            body.add("client_id", clientId)
            body.add("client_secret", clientSecret)
            body.add("token", token)
            RequestEntity<Any?>(body, headers, HttpMethod.POST, URI.create(introspectUri))
        }
        return opaqueTokenIntrospector
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource = CorsConfiguration()
        .apply {
            applyPermitDefaultValues()
            allowedOrigins = listOf("*")
            allowedMethods = listOf("*")
            allowedHeaders = listOf("*")
        }
        .let { corsConfig ->
            UrlBasedCorsConfigurationSource().apply {
                registerCorsConfiguration("/**", corsConfig)
            }
        }

    @Bean
    fun restClient() = RestClient.create()

}
