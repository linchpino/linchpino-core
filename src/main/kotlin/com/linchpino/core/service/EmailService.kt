package com.linchpino.core.service

import jakarta.mail.internet.InternetAddress
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
