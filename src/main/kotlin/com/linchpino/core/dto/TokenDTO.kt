package com.linchpino.core.dto

import java.time.Instant

data class TokenResponse(val token: String, val expiresAt: Instant?)
