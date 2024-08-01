package com.linchpino.core.service

import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Interview
import com.linchpino.core.entity.MentorTimeSlot
import com.linchpino.core.enums.AccountStatusEnum
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mail.javamail.JavaMailSender
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine
import java.time.ZonedDateTime
import java.util.Properties

@ExtendWith(MockitoExtension::class)
class EmailServiceTest {

    @InjectMocks
    private lateinit var service: EmailService

    @Mock
    private lateinit var javaMailSender: JavaMailSender

    @Mock
    private lateinit var springTemplateEngine: SpringTemplateEngine

    @Test
    fun `test sending interview invitation email`() {
        // Given
        val interview = Interview().apply {
            id = 1
            jobSeekerAccount = Account().apply {
                firstName = "John"
                lastName = "Doe"
            }
            timeSlot = MentorTimeSlot().apply {
                fromTime = ZonedDateTime.now().plusDays(2)
                toTime = ZonedDateTime.now().plusDays(2).plusMinutes(30)
            }
            jobSeekerAccount = Account().apply {
                status = AccountStatusEnum.DEACTIVATED
                externalId = "randomUUID"
                email = "john.doe@gmail.com"
            }

        }

        val templateNameCaptor: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        val contextCaptor: ArgumentCaptor<Context> = ArgumentCaptor.forClass(Context::class.java)

        val mimeMessage = MimeMessage(Session.getInstance(Properties()))
        `when`(springTemplateEngine.process(any(String::class.java), any(Context::class.java))).thenReturn("something")
        `when`(javaMailSender.createMimeMessage()).thenReturn(mimeMessage)

        service.sendingInterviewInvitationEmailToJobSeeker(interview)

        verify(springTemplateEngine, times(1)).process(templateNameCaptor.capture(), contextCaptor.capture())
        verify(javaMailSender, times(1)).createMimeMessage()
        verify(javaMailSender, times(1)).send(mimeMessage)

        assertThat(templateNameCaptor.value).isEqualTo("jobseeker-email-template.html")
        assertThat(contextCaptor.value.variableNames).isEqualTo(
            setOf(
                "fullName",
                "date",
                "time",
                "timezone",
                "isJobSeekerIsNotActivated",
                "applicationUrl",
                "interviewId",
                "jobSeekerExternalId"
            )
        )
    }

    @Test
    fun `test generate template`() {
        // Given
        val templateNameCaptor: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        val contextCaptor: ArgumentCaptor<Context> = ArgumentCaptor.forClass(Context::class.java)
        val model: Map<String, Any?> = mapOf("key" to "value")
        val templateName = "templateName.html"

        `when`(springTemplateEngine.process(any(String::class.java), any(Context::class.java))).thenReturn("something")

        // When
        service.generateTemplate(model, templateName)

        // Then
        verify(springTemplateEngine, times(1)).process(templateNameCaptor.capture(), contextCaptor.capture())
        assertThat(contextCaptor.value.variableNames).isEqualTo(model.keys)
        assertThat(templateNameCaptor.value).isEqualTo(templateName)
    }

    @Test
    fun `test sending welcome email to the registered mentor`() {
        // Given
        val account = Account().apply {
            firstName = "Mahsa"
            lastName = "Saeedi"
            email = "mahsa.saeedy@gmail.com"
        }

        val templateNameCaptor: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        val contextCaptor: ArgumentCaptor<Context> = ArgumentCaptor.forClass(Context::class.java)

        val mimeMessage = MimeMessage(Session.getInstance(Properties()))
        `when`(springTemplateEngine.process(any(String::class.java), any(Context::class.java))).thenReturn("something")
        `when`(javaMailSender.createMimeMessage()).thenReturn(mimeMessage)

        account.firstName?.let { firstName ->
            account.lastName?.let { lastName ->
                service.sendingWelcomeEmailToMentor(
                    firstName,
                    lastName,
                    account.email
                )
            }
        }

        verify(springTemplateEngine, times(1)).process(templateNameCaptor.capture(), contextCaptor.capture())
        verify(javaMailSender, times(1)).createMimeMessage()
        verify(javaMailSender, times(1)).send(mimeMessage)

        assertThat(templateNameCaptor.value).isEqualTo("mentor-email-template.html")
        assertThat(contextCaptor.value.variableNames).isEqualTo(
            setOf(
                "fullName",
            )
        )
    }
}
