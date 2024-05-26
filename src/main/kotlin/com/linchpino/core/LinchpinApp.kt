package com.linchpino.core

import com.linchpino.core.security.RSAKeys
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication(scanBasePackages = ["com.linchpino"])
@EnableJpaAuditing
@EnableConfigurationProperties(RSAKeys::class)
@OpenAPIDefinition(info = Info(title = "Linchpino Core", version = "1.0"))
class LinchpinApp

fun main(args: Array<String>) {
    runApplication<LinchpinApp>(*args)
}
