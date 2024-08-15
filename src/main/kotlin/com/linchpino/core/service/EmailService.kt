package com.linchpino.core.service

import com.linchpino.core.entity.Interview
import com.linchpino.core.entity.interviewPartiesFullName
import com.linchpino.core.enums.AccountStatusEnum
import jakarta.mail.internet.InternetAddress
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.InputStreamSource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class EmailService(
    private val emailSender: JavaMailSender,
    private val templateEngine: SpringTemplateEngine
) {
    @Value("\${application.url}")
    var applicationUrl: String? = null

    @Value("\${spring.mail.username}")
    var mailUsername: String? = null

    @Value("\${email.from.name}")
    var mailFromName: String? = null

    @Value("\${email.from.mailAddress}")
    var mailFrom: String? = null


    private val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.X")

    fun sendingWelcomeEmailToMentor(firstName: String, lastName: String, email: String) {
        val fullName = "$firstName $lastName"

        val templateContextData = mapOf(
            "fullName" to fullName,
        )
        sendEmail(
            email,
            "Welcome to Linchpino - Confirmation of Mentor Registration",
            "mentor-email-template.html",
            templateContextData,
        )
    }

    fun sendingInterviewInvitationEmailToJobSeeker(interview: Interview) {

        val (mentorFullName, jobSeekerFullName) = interview.interviewPartiesFullName()

        val templateContextData =
            mapOf(
                "fullName" to jobSeekerFullName,
                "date" to interview.timeSlot?.fromTime?.toLocalDate(),
                "time" to interview.timeSlot?.fromTime?.toLocalTime(),
                "timezone" to interview.timeSlot?.fromTime?.zone,
                "isJobSeekerIsNotActivated" to (interview.jobSeekerAccount?.status != AccountStatusEnum.ACTIVATED),
                "applicationUrl" to applicationUrl.toString(),
                "interviewId" to interview.id.toString(),
                "jobSeekerExternalId" to interview.jobSeekerAccount?.externalId
            )

        val attachment: InputStreamSource = ByteArrayResource(
            invitationFile(
                mentorFullName,
                interview.mentorAccount?.email,
                jobSeekerFullName,
                interview.jobSeekerAccount?.email,
                interview.timeSlot?.toTime?.format(dateTimeFormat),
                interview.timeSlot?.fromTime?.format(dateTimeFormat),
                "Confirmation of Interview Schedule on Linchpino"
            ).toByteArray()
        )

        sendEmail(
            interview.jobSeekerAccount!!.email,
            "Confirmation of Interview Schedule on Linchpino",
            "jobseeker-email-template.html",
            templateContextData,
            Attachment("invite.ics", attachment, "text/calendar; charset=UTF-8")
        )
    }


    fun sendEmail(
        to: String,
        subject: String,
        templateName: String,
        model: Map<String, Any?>,
        attachment: Attachment? = null
    ) {
        val htmlContent = generateTemplate(model, templateName)
        val message = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(htmlContent, true)
        helper.setFrom(InternetAddress(mailFrom, mailFromName))

        attachment?.let {
            helper.addAttachment(it.fileName, it.file, it.contentType)
        }

        emailSender.send(message)
    }

    fun generateTemplate(model: Map<String, Any?>, templateName: String): String {
        val context = Context().apply {
            setVariables(model)
        }

        return templateEngine.process(templateName, context)
    }

    private fun invitationFile(
        mentorFullName: String,
        mentorEmail: String?,
        jobSeekerFullName: String,
        jobSeekerEmail: String?,
        toDate: String?,
        fromDate: String?,
        summary: String
    ): String {
        return """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-///Linchpino//EN
            CALSCALE:GREGORIAN
            METHOD:REQUEST
            BEGIN:VEVENT
            UID:1234567890
            DTSTAMP:${ZonedDateTime.now(ZoneOffset.UTC).format(dateTimeFormat)}
            DTSTART:$fromDate
            DTEND:$toDate
            SUMMARY:$summary
            LOCATION:Online
            ORGANIZER;CN=Linchpino:mailto:$mailUsername
            ATTENDEE;RSVP=TRUE;CN=$jobSeekerFullName Name:mailto:$jobSeekerEmail
            ATTENDEE;RSVP=TRUE;CN=$mentorFullName Name:mailto:$mentorEmail
            END:VEVENT
            END:VCALENDAR
            """.trimIndent()
    }

    data class Attachment(
        val fileName: String,
        val file: InputStreamSource,
        val contentType: String,
    )
}
