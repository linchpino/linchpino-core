package com.linchpino.core.service

import com.linchpino.core.dto.LinkedInUserInfoResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpHeaders
import org.springframework.web.client.RestClient

@ExtendWith(MockitoExtension::class)
class LinkedInServiceTest{

    @Mock
    private lateinit var restClient: RestClient

    @InjectMocks
    private lateinit var linkedInService: LinkedInService

    @Test
    fun `should return LinkedInUserInfoResponse when API call is successful`() {
        // Given
        val token = "valid_token"
        val expectedResponse = LinkedInUserInfoResponse("john.doe@example.com","john","doe")

        val requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec::class.java)
        val requestHeadersSpec = mock(RestClient.RequestHeadersSpec::class.java)
        val responseSpec = mock(RestClient.ResponseSpec::class.java)


        `when`(restClient.get()).thenReturn(requestHeadersUriSpec)
        `when`(requestHeadersUriSpec.uri("https://api.linkedin.com/v2/userinfo")).thenReturn(requestHeadersSpec)
        `when`(requestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer $token")).thenReturn(requestHeadersSpec)
        `when`(requestHeadersSpec.retrieve()).thenReturn(responseSpec)
        `when`(responseSpec.body(LinkedInUserInfoResponse::class.java)).thenReturn(expectedResponse)

        // When
        val result = linkedInService.userInfo(token)

        // Then
        assertThat(result).isEqualTo(expectedResponse)
    }

    @Test
    fun `should fail when API call is not successful`() {
        // Given
        val token = "valid_token"

        val requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec::class.java)
        val requestHeadersSpec = mock(RestClient.RequestHeadersSpec::class.java)
        val responseSpec = mock(RestClient.ResponseSpec::class.java)


        `when`(restClient.get()).thenReturn(requestHeadersUriSpec)
        `when`(requestHeadersUriSpec.uri("https://api.linkedin.com/v2/userinfo")).thenReturn(requestHeadersSpec)
        `when`(requestHeadersSpec.header(HttpHeaders.AUTHORIZATION, "Bearer $token")).thenReturn(requestHeadersSpec)
        `when`(requestHeadersSpec.retrieve()).thenReturn(responseSpec)
        `when`(responseSpec.body(LinkedInUserInfoResponse::class.java)).thenReturn(null)

        assertThrows(RuntimeException::class.java) {
            linkedInService.userInfo(token)
        }
    }
}
