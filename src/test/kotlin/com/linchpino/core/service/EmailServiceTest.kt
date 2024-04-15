package com.linchpino.core.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.mail.javamail.JavaMailSender
import org.thymeleaf.context.Context
import org.thymeleaf.spring5.SpringTemplateEngine

class EmailServiceTest {
    private val TO = "mahsa.saeedy@gmail.com"
    private val TEMPLATE_NAME = "email-template.html"

    @Mock
    private lateinit var emailSender: JavaMailSender

    @Mock
    private lateinit var templateEngine: SpringTemplateEngine

    @InjectMocks
    private lateinit var emailService: EmailService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testSendTemplateEmail() {
        val model = mapOf(
            "fullName" to "Mahsa Saeedi",
            "Date" to "",
            "Time" to "",
            "Timezone" to "",
            "interviewLink" to "",
            "registrationLink" to "",
        )

        val expectedHtmlContent = "<html><body><h1>Hello, Test User!</h1></body></html>"

        `when`(templateEngine.process(TEMPLATE_NAME, Context().apply {
            model.forEach { (key, value) -> setVariable(key, value) }
        })).thenReturn(expectedHtmlContent)

        emailService.sendTemplateEmail(TO, TEMPLATE_NAME, model)
    }
}
