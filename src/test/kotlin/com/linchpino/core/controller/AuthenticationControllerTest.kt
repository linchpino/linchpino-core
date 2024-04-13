package com.linchpino.core.controller

import com.linchpino.core.captureNonNullable
import com.linchpino.core.dto.TokenResponse
import com.linchpino.core.security.JWTService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class AuthenticationControllerTest {

    @Mock
    private lateinit var jwtService: JWTService

    @InjectMocks
    private lateinit var authenticationController: AuthenticationController

    @Test
    fun `test login`() {
        // Given
        val authentication = UsernamePasswordAuthenticationToken(
            "john@example.com", "secret", mutableListOf(
                SimpleGrantedAuthority("user"),
                SimpleGrantedAuthority("admin")
            )
        )
        val authCaptor: ArgumentCaptor<Authentication> = ArgumentCaptor.forClass(Authentication::class.java)
        val expectedResponse = TokenResponse("fake token", Instant.now())
        `when`(jwtService.token(authCaptor.captureNonNullable())).thenReturn(expectedResponse)

        // When
        val result = authenticationController.login(authentication)

        // Then
        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(result.body).isEqualTo(expectedResponse)
        assertThat(authCaptor.value).isEqualTo(authentication)
        verify(jwtService, times(1)).token(authentication)
    }
}
