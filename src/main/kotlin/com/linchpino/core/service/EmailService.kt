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

    fun sendEmail(interview: Interview) {
        val fullName = "${interview.jobSeekerAccount!!.firstName} ${interview.jobSeekerAccount!!.lastName}"
        val date = interview.timeSlot!!.fromTime.toLocalDate()
        val time = interview.timeSlot!!.fromTime.toLocalTime()
        val zone = interview.timeSlot!!.fromTime.getZone()

        val templateContextData = mapOf(
            "fullName" to fullName,
            "date" to date,
            "time" to time,
            "timezone" to zone,
            "isJobSeekerActivated" to (interview.jobSeekerAccount!!.status == AccountStatusEnum.ACTIVATED),
            "applicationUrl" to applicationUrl.toString(),
            "interviewId" to interview.id.toString()
        )

        val mailFrom: String? = environment.getProperty("spring.mail.properties.mail.smtp.from")
        val mailFromName: String = environment.getProperty("mail.from.name", "Linchpino")

        sendTemplateEmail(
            InternetAddress(mailFrom, mailFromName),
            interview.jobSeekerAccount!!.email,
            "Confirmation of Interview Schedule on Linchpino",
            "jobseeker-jobseeker-email-template.html",
            templateContextData
        )
    }

    fun sendTemplateEmail(
        from: InternetAddress,
        to: String,
        subject: String,
        templateName: String,
        model: Map<String, Any>
    ) {
        val context = Context().apply {
            setVariables(model)
        }

        val htmlContent = templateEngine.process(templateName, context)
        val message = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(htmlContent, true)
        helper.setFrom(from)

        emailSender.send(message)
    }
}
