package com.linchpino.core.service

import com.linchpino.core.enums.AccountStatusEnum
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.mail.javamail.JavaMailSender
import org.thymeleaf.spring6.SpringTemplateEngine

class EmailServiceTest {
    private val emailSender = mock(JavaMailSender::class.java)
    private val templateEngine = mock(SpringTemplateEngine::class.java)
    private val emailService = EmailService(emailSender, templateEngine)

    @Test
    fun `test sendTemplateEmail`() {
        val from = InternetAddress("test@example.com")
        val to = "mahsa.saeedy@gmail.com"
        val subject = "Test Email"
        val templateName = "email-template"
        val model = mapOf(
            "fullName" to "Mahsa Saeedi",
            "date" to "2024/05/09",
            "time" to "16:30",
            "timezone" to "CET",
            "isJobSeekerActivated" to AccountStatusEnum.ACTIVATED,
            "applicationUrl" to "https://linchpino.liara.run",
            "interviewId" to 1
        )

        val expectedHtmlContent = """<div xmlns:th="http://www.w3.org/1999/xhtml">
    <p>Dear <b th:text=" ${model.get("fullName")}"></b>,</p>
    <p>Thank you for choosing Linchpino to schedule your upcoming interview.</p>
    <p>Your interview is scheduled for <b th:text=" ${model.get("date")} + ' at ' +  ${model.get("time")} + ' ' +  ${
            model.get(
                "timezone"
            )
        }}"></b>.</p>
    <p>Please ensure you are ready to join the interview at least 10 minutes before the scheduled time.</p>
    <p>Here is the link to access your interview:</p>
    <a th:href=" ${model.get("applicationUrl")} + '/interview/view/' +  ${model.get("interviewId")}"><span th:text="${
            model.get(
                "applicationUrl"
            )
        } + '/interview/view/' + interviewId}"></span></a>
    <div th:if=" ${model.get("isJobSeekerActivated")}">
        <p>We also kindly ask you to activate your account through the following link:</p>
        <a th:href="${model.get("applicationUrl")} + '/jobseeker-activation'}"><span th:text="${model.get("applicationUrl")} + '/jobseeker-activation'}"></span></a>
        <p>Registering will allow you to access your previous interview/s, receive updates, and make future interview
            scheduling easier.</p>
    </div>
    <p>If you have any questions or need further assistance, please don't hesitate to reach out to us
        mailto:support@linchpino.com.</p>
    <p>Best Regards,</p>
    <p>Linchpino Team</p>
</div>"""

        `when`(templateEngine.process(eq(templateName), any())).thenReturn(expectedHtmlContent)

        val mimeMessage = mock(MimeMessage::class.java)
        `when`(emailSender.createMimeMessage()).thenReturn(mimeMessage)

        emailService.sendTemplateEmail(from, to, subject, templateName, model)

        verify(emailSender).createMimeMessage()
        verify(emailSender).send(mimeMessage)
        verify(templateEngine).process(eq(templateName), any())
    }
}
