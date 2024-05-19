package com.linchpino.core.service

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

    fun sendTemplateEmail(to: String, templateName: String, model: Map<String, Any>) {
        val context = Context().apply {
            model.forEach { (key, value) -> setVariable(key, value) }
        }

        val htmlContent = templateEngine.process(templateName, context)
        val message = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")
        helper.setTo(to)
        helper.setSubject("Confirmation of Interview Schedule on Linchpino")
        helper.setText(htmlContent, true)

        emailSender.send(message)
    }
}
