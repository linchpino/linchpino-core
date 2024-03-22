package com.linchpino.core

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class LinchpinApp

fun main(args: Array<String>) {
	runApplication<LinchpinApp>(*args)
}
