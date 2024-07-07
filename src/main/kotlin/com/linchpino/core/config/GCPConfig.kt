package com.linchpino.core.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource

@Configuration
class GCPConfig {

    @Bean
    fun storage(
        @Value("\${gcp.projectId}")
        projectId: String,
        @Value("\${meet.credential}")
        credential: Resource
    ): Storage {
        val storage: Storage = StorageOptions.newBuilder()
            .setCredentials(GoogleCredentials.fromStream(credential.inputStream))
            .setProjectId(projectId).build().service
        return storage
    }
}
