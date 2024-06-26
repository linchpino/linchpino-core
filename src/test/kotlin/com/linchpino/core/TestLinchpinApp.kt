package com.linchpino.core

import com.linchpino.LinchpinApp
import org.springframework.boot.fromApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.with
import org.springframework.context.annotation.Import

@TestConfiguration(proxyBeanMethods = false)
@Import(PostgresContainerConfig::class)
class TestLinchpinApp

fun main(args: Array<String>) {
    fromApplication<LinchpinApp>().with(TestLinchpinApp::class).run(*args)
}
