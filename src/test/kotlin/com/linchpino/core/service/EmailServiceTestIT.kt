package com.linchpino.core.service

import com.linchpino.core.PostgresContainerConfig
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(PostgresContainerConfig::class)
class EmailServiceTestIT {
    @Autowired
    private lateinit var emailService: EmailService

    @Test
    fun `test generate jobseeker's template with active account`() {
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
    fun `test generate jobseeker's template when account deactivate`() {
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

    @Test
    fun `test generate mentor's template`() {
        val templateName = "mentor-email-template"
        val model = mapOf(
            "fullName" to "Mentor",
        )

        val expectedHtmlContent = """<div>
    <p>Dear <b>${model["fullName"]}</b>,</p>
    <p>Thank you for registering as a mentor on Linchpino! We're glad to have you join our community of mentors
        dedicated to empowering and guiding individuals towards their goals.</p>
    <div>
        <div>Your registration has been successfully processed, and you are now ready to offer your expertise and
            support
            to those seeking mentorship.
            As a mentor on our platform, you have the flexibility to offer your services in three different forms:
        </div>
        <ul>
            <li>
                <strong>Free of Charge:</strong> You can choose to provide your mentorship services free of charge,
                offering guidance and support to individuals who may benefit from your knowledge without any financial
                commitment.
            </li>
            <li>
                <strong>Fixed Price:</strong> Set a fixed price for your mentorship services, allowing mentees to
                engage
                with you at a rate that reflects the value of your expertise and time.
            </li>
            <li>
                <strong>Pay What You Want:</strong> Provide the option for mentees to pay as much as they want for
                your
                mentorship services, giving them the freedom to contribute an amount that they feel is fair and
                reflective of the value they receive.
            </li>
        </ul>
    </div>
    <p>We believe that offering these diverse options will enable you to connect with a wide range of mentees and make
        a meaningful impact in their lives.</p>
    <p>Please take a moment to review your mentor profile and ensure that all the information is accurate and up to
        date.
        If you have any questions or need assistance, feel free to reach out to our support team at
        mailto:support@linchpino.com.
    </p>
    <p>Once again, thank you for choosing to be a part of our mentorship community. We look forward to seeing the
        positive impact you will make as a mentor!
    </p>
    <p>Best Regards,</p>
    <p>Linchpino Team</p>
</div>
"""

        val generatedTemplate = emailService.generateTemplate(model, templateName)

        assertThat(generatedTemplate.replace("\n", "").replace("\r", ""))
            .isEqualTo(expectedHtmlContent.replace("\n", "").replace("\r", ""))
    }
}
