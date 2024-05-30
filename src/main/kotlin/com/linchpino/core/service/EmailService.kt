package com.linchpino.core.service

import com.linchpino.core.entity.Interview
import com.linchpino.core.enums.AccountStatusEnum
import jakarta.mail.internet.InternetAddress
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine

@Service
class EmailService(
    private val emailSender: JavaMailSender,
    private val templateEngine: SpringTemplateEngine,
    private val environment: Environment
) {
    @Value("\${application.url}")
    var applicationUrl: String? = null

    @Value("spring.mail.properties.mail.smtp.from")
    var mailFrom: String? = null

    @Value("mail.from.name")
    var mailFromName: String? = null

    fun sendingInterviewInvitationEmailToJobSeeker(interview: Interview) {
        val fullName = "${interview.jobSeekerAccount!!.firstName} ${interview.jobSeekerAccount!!.lastName}"
        val finalName = fullName.takeIf { it.isEmpty() } ?: "Jobseeker"
        val date = interview.timeSlot!!.fromTime.toLocalDate()
        val time = interview.timeSlot!!.fromTime.toLocalTime()
        val zone = interview.timeSlot!!.fromTime.getZone()
        val isJobSeekerIsNotActivated = interview.jobSeekerAccount!!.status != AccountStatusEnum.ACTIVATED
        val jobSeekerExternalId = interview.jobSeekerAccount!!.externalId.toString()

        val templateContextData = mapOf(
            "fullName" to finalName,
            "date" to date,
            "time" to time,
            "timezone" to zone,
            "isJobSeekerIsNotActivated" to isJobSeekerIsNotActivated,
            "applicationUrl" to applicationUrl.toString(),
            "interviewId" to interview.id.toString(),
            "jobSeekerExternalId" to jobSeekerExternalId
        )

        sendEmail(
            InternetAddress(mailFrom, mailFromName),
            interview.jobSeekerAccount!!.email,
            "Confirmation of Interview Schedule on Linchpino",
            "jobseeker-email-template.html",
            templateContextData
        )
    }

    internal fun sendEmail(
        from: InternetAddress,
        to: String,
        subject: String,
        templateName: String,
        model: Map<String, Any>
    ) {
        val htmlContent = generateTemplate(model, templateName)
        val message = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(htmlContent, true)
        helper.setFrom(from)

        emailSender.send(message)
    }

    internal fun generateTemplate(model: Map<String, Any>, templateName: String): String {

        val context = Context().apply {
            setVariables(model)
        }

        val htmlContent = templateEngine.process(templateName, context)
        return htmlContent
    }
}
