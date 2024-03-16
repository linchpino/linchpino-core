package com.linchpino.core

import com.linchpino.core.LinchpinApp
import org.springframework.boot.fromApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.with
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
@Import(PostgresContainerConfig::class)
class TestLinchpinApp

fun main(args: Array<String>) {
	fromApplication<LinchpinApp>().with(TestLinchpinApp::class).run(*args)
}


