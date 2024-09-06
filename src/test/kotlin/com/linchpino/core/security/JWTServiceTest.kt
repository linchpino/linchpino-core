package com.linchpino.core.security

import com.linchpino.core.enums.AccountTypeEnum
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.TemporalUnitWithinOffset
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder


class JWTServiceTest {

    private val keyPair = generateKeyPair()
    private val jwtService = JWTService(jwtEncoder(keyPair))
    private val jwtDecoder = jwtDecoder(keyPair)

    @Test
    fun `test creating token when authentication is provided`() {
        // Given
        val now = Instant.now()
        val authentication = UsernamePasswordAuthenticationToken(
            "john@example.com", "secret", mutableListOf(
                SimpleGrantedAuthority(AccountTypeEnum.MENTOR.name),
                SimpleGrantedAuthority(AccountTypeEnum.JOB_SEEKER.name)
            )
        )
        // When
        val tokenResponse = jwtService.token(authentication)

        // Then
        val decodedToken = jwtDecoder.decode(tokenResponse.token)
        assertThat(tokenResponse.expiresAt).isCloseTo(now.plus(1, ChronoUnit.HOURS),
            TemporalUnitWithinOffset(1,ChronoUnit.SECONDS)
        )
        assertThat(decodedToken.issuer.host).isEqualTo("linchpino.com")
        assertThat(decodedToken.subject).isEqualTo("john@example.com")
        assertThat(decodedToken.getClaim("scope") as String).isEqualTo("MENTOR JOB_SEEKER")
    }

    private fun generateKeyPair(): RSAKeys {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        val keyPair = generator.generateKeyPair()
        return RSAKeys(keyPair.public as RSAPublicKey, keyPair.private as RSAPrivateKey)
    }

    private fun jwtDecoder(keys: RSAKeys): NimbusJwtDecoder = NimbusJwtDecoder.withPublicKey(keys.publicKey).build()

    private fun jwtEncoder(keys: RSAKeys): NimbusJwtEncoder {
        val jwk = RSAKey.Builder(keys.publicKey).privateKey(keys.privateKey).build()
        return NimbusJwtEncoder(ImmutableJWKSet(JWKSet(jwk)))
    }
}
