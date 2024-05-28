package com.linchpino.core.security

import com.linchpino.core.entity.Role
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal
import org.springframework.web.client.RestClient


@ExtendWith(MockitoExtension::class)
//@RestClientTest
class LinkedInSecurityFilterTest {

    @Mock
    lateinit var restClient: RestClient

    @Mock
    lateinit var userService: UserService

    @Mock
    lateinit var filterChain: FilterChain

    private lateinit var linkedInSecurityFilter: LinkedInSecurityFilter

    private lateinit var request: MockHttpServletRequest

    private lateinit var response: MockHttpServletResponse


    @BeforeEach
    fun setUp() {
        linkedInSecurityFilter = LinkedInSecurityFilter(restClient, "http://localhost/somefakeuri", userService)
        MockitoAnnotations.openMocks(linkedInSecurityFilter)
        request = MockHttpServletRequest()
        response = MockHttpServletResponse()
        linkedInSecurityFilter = LinkedInSecurityFilter(restClient, "userInfoUri", userService)
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `doFilterInternal when no authentication should do nothing`() {
        linkedInSecurityFilter.doFilterInternal(request, response, filterChain)
        verify(filterChain, times(1)).doFilter(request, response)
    }

    @Test
    fun `doFilterInternal when BearerTokenAuthentication should update SecurityContext`() {
        val token = "valid-token"
        val email = "user@example.com"
        val principalAttributes = mapOf<String, Any>("key" to "value")
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val userInfoResponse = LinkedInUserInfoResponse("john.doe@example.com", "Test User")

        val principal = OAuth2IntrospectionAuthenticatedPrincipal(email, principalAttributes, authorities)
        val authentication = BearerTokenAuthentication(
            principal,
            OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, token, null, null),
            authorities
        )

        SecurityContextHolder.getContext().authentication = authentication

        val requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec::class.java)
        val requestHeadersSpec = mock(RestClient.RequestHeadersSpec::class.java)
        val responseSpec = mock(RestClient.ResponseSpec::class.java)

        `when`(restClient.get()).thenReturn(requestHeadersUriSpec)
        `when`(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec)
        `when`(requestHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestHeadersSpec)
        `when`(requestHeadersSpec.retrieve()).thenReturn(responseSpec)
        `when`(responseSpec.body(LinkedInUserInfoResponse::class.java)).thenReturn(userInfoResponse)


        `when`(userService.loadUserByUsername(userInfoResponse.email)).thenReturn(SecurityUser(setOf(Role().apply {
            title = AccountTypeEnum.MENTOR
        }), userInfoResponse.email, "password", AccountStatusEnum.ACTIVATED))

        linkedInSecurityFilter.doFilterInternal(request, response, filterChain)

        val newAuthentication = SecurityContextHolder.getContext().authentication
        assert(newAuthentication is BearerTokenAuthentication)
        assert(newAuthentication!!.name == "john.doe@example.com")

        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `doFilterInternal when user not found should log error`() {
        val token = "valid-token"
        val email = "user@example.com"
        val principalAttributes = mapOf<String, Any>("key" to "value")
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val userInfoResponse = LinkedInUserInfoResponse(email, "Test User")

        val principal = OAuth2IntrospectionAuthenticatedPrincipal(email, principalAttributes, authorities)
        val authentication = BearerTokenAuthentication(
            principal,
            OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, token, null, null),
            authorities
        )

        SecurityContextHolder.getContext().authentication = authentication

        val requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec::class.java)
        val requestHeadersSpec = mock(RestClient.RequestHeadersSpec::class.java)
        val responseSpec = mock(RestClient.ResponseSpec::class.java)

        `when`(restClient.get()).thenReturn(requestHeadersUriSpec)
        `when`(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec)
        `when`(requestHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestHeadersSpec)
        `when`(requestHeadersSpec.retrieve()).thenReturn(responseSpec)
        `when`(responseSpec.body(LinkedInUserInfoResponse::class.java)).thenReturn(userInfoResponse)

        `when`(userService.loadUserByUsername(email)).thenThrow(UsernameNotFoundException(email))

        linkedInSecurityFilter.doFilterInternal(request, response, filterChain)

        verify(filterChain).doFilter(request, response)
        verify(userService).loadUserByUsername(email)
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun clearContext() {
            SecurityContextHolder.clearContext()
        }
    }
}
