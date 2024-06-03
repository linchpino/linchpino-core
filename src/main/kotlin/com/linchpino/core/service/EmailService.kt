package com.linchpino.core.service

import com.linchpino.core.entity.Interview
import com.linchpino.core.enums.AccountStatusEnum
import jakarta.mail.internet.InternetAddress
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine

@Service
class EmailService(
    private val emailSender: JavaMailSender,
    private val templateEngine: SpringTemplateEngine
) {
    @Value("\${application.url}")
    var applicationUrl: String? = null

    @Value("\${spring.mail.properties.mail.smtp.from}")
    var mailFrom: String? = null

    @Value("\${mail.from.name}")
    var mailFromName: String? = null

    fun sendingInterviewInvitationEmailToJobSeeker(interview: Interview) {
        val fullName =
            if (interview.jobSeekerAccount?.firstName == null || interview.jobSeekerAccount?.lastName == null) {
                "JobSeeker"
            } else {
                "${interview.jobSeekerAccount?.firstName} ${interview.jobSeekerAccount?.lastName}"
            }
        val date = interview.timeSlot?.fromTime?.toLocalDate()
        val time = interview.timeSlot?.fromTime?.toLocalTime()
        val zone = interview.timeSlot?.fromTime?.zone
        val isJobSeekerIsNotActivated = interview.jobSeekerAccount?.status != AccountStatusEnum.ACTIVATED
        val jobSeekerExternalId = interview.jobSeekerAccount?.externalId

        val templateContextData = mapOf(
            "fullName" to fullName,
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

    private fun sendEmail(
        from: InternetAddress,
        to: String,
        subject: String,
        templateName: String,
        model: Map<String, Any?>
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

    fun generateTemplate(model: Map<String, Any?>, templateName: String): String {

        val context = Context().apply {
            setVariables(model)
        }

        return templateEngine.process(templateName, context)
    }
}
