package com.linchpino.core.service

import com.linchpino.core.entity.Interview
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
import java.io.Serializable
import java.time.*
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

    @Value("\${email.jobSeekerSubject}")
    var jobSeekerSubject: String = ""

    @Value("\${email.mentorSubject}")
    var mentorSubject: String = ""

    private var fromDate: String? = null
    private var toDate: String? = null
    private var jobSeekerFullName: String? = null
    private var jobSeekerEmailAddress: String? = null
    private var mentorFullName: String? = null
    private var mentorEmailAddress: String? = null
    val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.X")

    fun sendingWelcomeEmailToMentor(firstName: String, lastName: String, email: String) {
        val fullName = "$firstName $lastName"

        val templateContextData = mapOf(
            "fullName" to fullName,
        )

        mentorFullName = fullName
        mentorEmailAddress = email

        sendEmail(
            null, null, null,
            InternetAddress(mailFrom, mailFromName),
            email,
            mentorSubject,
            "mentor-email-template.html",
            templateContextData
        )
    }

    fun sendingInterviewInvitationEmailToJobSeeker(interview: Interview) {
        val fullName =
            if (interview.jobSeekerAccount?.firstName == null || interview.jobSeekerAccount?.lastName == null) {
                "JobSeeker"
            } else {
                "${interview.jobSeekerAccount?.firstName} ${interview.jobSeekerAccount?.lastName}"
            }

        interview.timeSlot?.fromTime?.let { fromDateTime(it) }
        interview.timeSlot?.toTime?.let { toDateTime(it) }
        jobSeekerFullName = fullName
        jobSeekerEmailAddress = interview.jobSeekerAccount?.email

        val date = interview.timeSlot?.fromTime?.toLocalDate()
        val time = interview.timeSlot?.fromTime?.toLocalTime()
        val zone = interview.timeSlot?.fromTime?.zone
        val isJobSeekerIsNotActivated = interview.jobSeekerAccount?.status != AccountStatusEnum.ACTIVATED
        val jobSeekerExternalId = interview.jobSeekerAccount?.externalId

        val templateContextData =
            jobSeekerEmailContextModel(
                fullName,
                date,
                time,
                zone,
                isJobSeekerIsNotActivated,
                interview,
                jobSeekerExternalId
            )

        val attachmentFilename = createICSContent()
        val attachment: InputStreamSource = ByteArrayResource(attachmentFilename.toByteArray())

        sendEmail(
            "invite.ics",
            attachment,
            "text/calendar; charset=UTF-8",
            InternetAddress(mailFrom, mailFromName),
            interview.jobSeekerAccount!!.email,
            jobSeekerSubject,
            "jobseeker-email-template.html",
            templateContextData
        )
    }

    private fun jobSeekerEmailContextModel(
        fullName: String,
        date: LocalDate?,
        time: LocalTime?,
        zone: ZoneId?,
        isJobSeekerIsNotActivated: Boolean,
        interview: Interview,
        jobSeekerExternalId: String?
    ): Map<String, Serializable?> {
        return mapOf(
            "fullName" to fullName,
            "date" to date,
            "time" to time,
            "timezone" to zone,
            "isJobSeekerIsNotActivated" to isJobSeekerIsNotActivated,
            "applicationUrl" to applicationUrl.toString(),
            "interviewId" to interview.id.toString(),
            "jobSeekerExternalId" to jobSeekerExternalId
        )
    }

    private fun sendEmail(
        attachmentFileName: String?,
        attachment: InputStreamSource?,
        attachmentContentType: String?,
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

        attachmentFileName?.let { fileName ->
            attachment?.let { icsContent ->
                attachmentContentType?.let { contentType ->
                    helper.addAttachment(fileName, icsContent, contentType)
                }
            }
        }

        emailSender.send(message)
    }

    fun generateTemplate(model: Map<String, Any?>, templateName: String): String {
        val context = Context().apply {
            setVariables(model)
        }

        return templateEngine.process(templateName, context)
    }

    private fun createICSContent(): String {
        return """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-///Linchpino//EN
            CALSCALE:GREGORIAN
            METHOD:REQUEST
            BEGIN:VEVENT
            UID:1234567890
            DTSTAMP:${currentDateTime()}
            DTSTART:$fromDate
            DTEND:$toDate
            SUMMARY:$jobSeekerSubject
            LOCATION:Online
            ORGANIZER;CN=Linchpino:mailto:$mailUsername
            ATTENDEE;RSVP=TRUE;CN=$jobSeekerFullName Name:mailto:$jobSeekerEmailAddress
            ATTENDEE;RSVP=TRUE;CN=$mentorFullName Name:mailto:$mentorEmailAddress
            END:VEVENT
            END:VCALENDAR
            """.trimIndent()
    }

    private fun fromDateTime(fromTime: ZonedDateTime) {
        fromDate = fromTime.format(dateTimeFormat)
    }

    private fun toDateTime(toTime: ZonedDateTime) {
        toDate = toTime.format(dateTimeFormat)
    }

    private fun currentDateTime(): String {
        val currentDateTime = ZonedDateTime.now(ZoneOffset.UTC)
        return currentDateTime.format(dateTimeFormat)
    }
}
