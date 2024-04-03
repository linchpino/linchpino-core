package com.linchpino.core.security

import org.springframework.boot.context.properties.ConfigurationProperties
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

@ConfigurationProperties(prefix = "rsa")
data class RSAKeys(val publicKey: RSAPublicKey, val privateKey: RSAPrivateKey)
