package com.linchpino.core

import org.mockito.ArgumentCaptor

// use this helper to capture non-nullable kotlin types
object NonNullableArgumentCaptor {
    fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()
}
