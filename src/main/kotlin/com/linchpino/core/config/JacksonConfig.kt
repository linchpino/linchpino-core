package com.linchpino.core.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Configuration
class JacksonConfig(
    @Value("\${application.default-zone}")
    private val defaultZone: String) {

    @Bean
    fun objectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper {
        val objectMapper = builder.createXmlMapper(false).build<ObjectMapper>()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.registerModule(isoDateTimeModule())
        return objectMapper
    }

    private fun isoDateTimeModule(): SimpleModule {
        val defaultZoneId = ZoneId.of(defaultZone)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX").withZone(defaultZoneId)
//        val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME.withZone(defaultZoneId)
        val module = SimpleModule()
        module.addSerializer(ZonedDateTime::class.java, ZonedDateTimeSerializer(formatter))
        return module
    }
}
