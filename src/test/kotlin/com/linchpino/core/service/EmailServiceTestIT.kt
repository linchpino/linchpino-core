package com.linchpino.core.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest
class EmailServiceTestIT {
    @Autowired
    private lateinit var emailService  : EmailService

    @Test
    fun `test generate template with active account`() {
        val templateName = "jobseeker-email-template"
        val jobSeekerExternalId = UUID.randomUUID().toString()
        val model = mapOf(
            "fullName" to "Mahsa Saeedi",
            "date" to "2024/05/09",
            "time" to "16:30",
            "timezone" to "CET",
            "isJobSeekerIsNotActivated" to false,
            "applicationUrl" to "https://linchpino.liara.run",
            "interviewId" to 1,
            "jobSeekerExternalId" to jobSeekerExternalId,
        )

        val expectedHtmlContent = """<div>
    <p>Dear <b>${model["fullName"]}</b>,</p>
    <p>Thank you for choosing Linchpino to schedule your upcoming interview.</p>
    <p>Your interview is scheduled for <b>${model["date"]} at ${model["time"]} ${model["timezone"]}</b>.</p>
    <p>Please ensure you are ready to join the interview at least 10 minutes before the scheduled time.</p>
    <p>Here is the link to access your interview:</p>
    <a href="${model["applicationUrl"]}/interview/view/${model["interviewId"]}"><span>${model["applicationUrl"]}/interview/view/${model["interviewId"]}</span></a>
        <p>If you have any questions or need further assistance, please don't hesitate to reach out to us
        mailto:support@linchpino.com.</p>
    <p>Best Regards,</p>
    <p>Linchpino Team</p>
</div>
"""

        val generatedTemplate = emailService.generateTemplate(model, templateName)

        assertThat(generatedTemplate.replace("\n", "").replace("\r", ""))
            .isEqualTo(expectedHtmlContent.replace("\n", "").replace("\r", ""))
    }

    @Test
    fun `test generate template when account deactivate`() {
        val templateName = "jobseeker-email-template"
        val jobSeekerExternalId = UUID.randomUUID().toString()
        val model = mapOf(
            "fullName" to "Jobseeker",
            "date" to "2024/05/10",
            "time" to "16:30",
            "timezone" to "CET",
            "isJobSeekerIsNotActivated" to true,
            "applicationUrl" to "https://linchpino.liara.run",
            "interviewId" to 1,
            "jobSeekerExternalId" to jobSeekerExternalId,
        )

        val expectedHtmlContent = """<div>
    <p>Dear <b>Jobseeker</b>,</p>
    <p>Thank you for choosing Linchpino to schedule your upcoming interview.</p>
    <p>Your interview is scheduled for <b>${model["date"]} at ${model["time"]} ${model["timezone"]}</b>.</p>
    <p>Please ensure you are ready to join the interview at least 10 minutes before the scheduled time.</p>
    <p>Here is the link to access your interview:</p>
    <a href="${model["applicationUrl"]}/interview/view/${model["interviewId"]}"><span>${model["applicationUrl"]}/interview/view/${model["interviewId"]}</span></a>
    <div>
        <p>We also kindly ask you to activate your account through the following link:</p>
        <a href="${model["applicationUrl"]}/jobseeker-activation/${jobSeekerExternalId}"><span>${model["applicationUrl"]}/jobseeker-activation/${jobSeekerExternalId}</span></a>
        <p>Registering will allow you to access your previous interview/s, receive updates, and make future interview
            scheduling easier.</p>
    </div>
    <p>If you have any questions or need further assistance, please don't hesitate to reach out to us
        mailto:support@linchpino.com.</p>
    <p>Best Regards,</p>
    <p>Linchpino Team</p>
</div>
"""

        val generatedTemplate = emailService.generateTemplate(model, templateName)

        assertThat(generatedTemplate.replace("\n", "").replace("\r", ""))
            .isEqualTo(expectedHtmlContent.replace("\n", "").replace("\r", ""))
    }
}
