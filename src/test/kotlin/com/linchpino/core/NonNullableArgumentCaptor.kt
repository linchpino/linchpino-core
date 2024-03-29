package com.linchpino.core

import org.mockito.ArgumentCaptor

// use this helper to capture non-nullable kotlin types

fun <T> ArgumentCaptor<T>.captureNonNullable():T = this.capture()
