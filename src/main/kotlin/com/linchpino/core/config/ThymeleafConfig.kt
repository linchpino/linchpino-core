package com.linchpino.core.config

import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver


@Configuration
class ThymeleafConfig(
    private val applicationContext: ApplicationContext
) {
    @Bean
    fun templateEngine(): SpringTemplateEngine? {
        val templateEngine = SpringTemplateEngine()
        templateEngine.setTemplateResolver(templateResolver())
        templateEngine.enableSpringELCompiler = true
        templateEngine.setTemplateEngineMessageSource(messageSource())
        return templateEngine
    }

    private fun templateResolver(): ITemplateResolver? {
        val resolver = SpringResourceTemplateResolver()
        resolver.setApplicationContext(applicationContext)
//        resolver.prefix = "/resources/templates/"
//        resolver.suffix = ".html"
//        resolver.templateMode = TemplateMode.HTML
//        resolver.characterEncoding = "UTF-8"
        return resolver
    }

    @Bean
    fun messageSource(): MessageSource? {
        val messageSource = ReloadableResourceBundleMessageSource()
        messageSource.setBasename("classpath:application")
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }
}
