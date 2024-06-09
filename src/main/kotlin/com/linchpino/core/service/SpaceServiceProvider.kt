package com.linchpino.core.service

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.apps.meet.v2.SpacesServiceClient
import com.google.apps.meet.v2.SpacesServiceSettings
import com.google.auth.oauth2.ServiceAccountCredentials
import com.linchpino.core.config.MeetCredential
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.IOException

@Service
class SpaceServiceProvider(meetCredential: MeetCredential) {
    private val json = """
            {
            "type": "service_account",
            "private_key_id": "${meetCredential.privateKeyId}",
            "private_key": "${meetCredential.privateKey}",
            "client_id": "${meetCredential.clientId}",
            "client_email": "${meetCredential.clientEmail}"
            }
        """.trimIndent()

    fun spaceServiceClient(): SpacesServiceClient {

        val credentials = ServiceAccountCredentials.fromStream(ByteArrayInputStream(json.toByteArray()))
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
