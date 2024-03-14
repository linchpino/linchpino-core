package com.linchpino.core

import com.linchpino.core.dto.AccountDto
import com.linchpino.core.service.impl.AccountService
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class LinchpinApp

fun main(args: Array<String>) {
    runApplication<LinchpinApp>(*args)
}
