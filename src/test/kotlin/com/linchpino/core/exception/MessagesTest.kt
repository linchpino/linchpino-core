package com.linchpino.core.exception

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import java.util.*

class MessagesTest {

    @Test
    fun `test a message is provided for each error code`() {
        val messageSource = getMessageSource()
        ErrorCode.entries.forEach {
            val key = it.name
            val expectedMessage = messageSource.getMessage(key, null, Locale.ENGLISH)
            assertThat(expectedMessage).isNotBlank()
        }
    }

    private fun getMessageSource(): MessageSource {
        val messageSource = ReloadableResourceBundleMessageSource()
        messageSource.setBasename("classpath:messages")
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }
}
