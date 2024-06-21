package com.linchpino.core.service

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.apps.meet.v2.SpacesServiceClient
import com.google.apps.meet.v2.SpacesServiceSettings
import com.google.auth.oauth2.ServiceAccountCredentials
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import org.springframework.stereotype.Service
import java.io.IOException
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource

@Service
class SpaceServiceProvider(
    @Value("\${meet.credential}") private val credential: Resource
) {

    fun spaceServiceClient(): SpacesServiceClient {

        val credentials =
            ServiceAccountCredentials.fromStream(credential.inputStream)
                .createScoped("https://www.googleapis.com/auth/meetings.space.created")
                .createDelegated("linchpino@linchpino.com")

        val settings = SpacesServiceSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            .build()
        try {
            return SpacesServiceClient.create(settings)
        } catch (e: IOException) {
            throw LinchpinException(ErrorCode.SERVER_ERROR, "failed to create google meet")
        }
    }
}
