package com.linchpino.core.service

import com.linchpino.core.config.ThymeleafConfig
import jakarta.mail.internet.InternetAddress
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.env.Environment
import org.springframework.mail.javamail.JavaMailSender
import org.thymeleaf.spring6.SpringTemplateEngine

@ExtendWith(MockitoExtension::class)

class EmailServiceTest {
    @Mock
    private lateinit var mailSender: JavaMailSender
    @Mock
    private lateinit var environment: Environment
    @Mock
    private lateinit var templateEngine : SpringTemplateEngine
    @InjectMocks
    private lateinit var emailService  : EmailService

    @BeforeEach
    fun setup() {

        val thymeleafConfig = ThymeleafConfig()
        templateEngine = thymeleafConfig.templateEngine()!!
    }

    @Test
    @Ignore
    fun `test generate template with active account`() {
        val from = InternetAddress("test@example.com")
        val to = "mahsa.saeedy@gmail.com"
        val subject = "Test Email"
        val templateName = "jobseeker-email-template"
        val model = mapOf(
            "fullName" to "Mahsa Saeedi",
            "date" to "2024/05/09",
            "time" to "16:30",
            "timezone" to "CET",
            "isJobSeekerIsNotActivated" to false,
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
    <p>If you have any questions or need further assistance, please don't hesitate to reach out to us
        mailto:support@linchpino.com.</p>
    <p>Best Regards,</p>
    <p>Linchpino Team</p>
</div>"""

        val generatedTemplate = emailService.generateTemplate(model, templateName)

        verify(templateEngine).process(eq(templateName), any())
        assertThat(generatedTemplate).isEqualTo(expectedHtmlContent)
    }

    @Test
    @Ignore
    fun `test generate template when account deactivate`() {
        val from = InternetAddress("test@example.com")
        val to = "mahsa.saeedy@gmail.com"
        val subject = "Test Email"
        val templateName = "jobseeker-email-template"
        val model = mapOf(
            "fullName" to "Jobseeker",
            "date" to "2024/05/10",
            "time" to "16:30",
            "timezone" to "CET",
            "isJobSeekerIsNotActivated" to true,
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
                 <p #if(${model.get("isJobSeekerActivated")})>
                <p>We also kindly ask you to activate your account through the following link:</p>
                <a th:href="${model.get("applicationUrl")} + '/jobseeker-activation'}"><span th:text="${model.get("applicationUrl")} + '/jobseeker-activation'}"></span></a>
                <p>Registering will allow you to access your previous interview/s, receive updates, and make future interview
                    scheduling easier.</p>
            </p>
            <p>If you have any questions or need further assistance, please don't hesitate to reach out to us
                mailto:support@linchpino.com.</p>
            <p>Best Regards,</p>
            <p>Linchpino Team</p>
        </div>"""


        val generatedTemplate = emailService.generateTemplate(model, templateName)

        verify(templateEngine).process(eq(templateName), any())
        assertThat(generatedTemplate).isEqualTo(expectedHtmlContent)
    }
}
