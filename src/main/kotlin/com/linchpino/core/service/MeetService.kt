package com.linchpino.core.service

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.apps.meet.v2.CreateSpaceRequest
import com.google.apps.meet.v2.Space
import com.google.apps.meet.v2.SpacesServiceClient
import com.google.apps.meet.v2.SpacesServiceSettings
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class MeetService {


    fun createGoogleWorkSpace(): Space? {

        val credentials = ServiceAccountCredentials.fromStream(ClassPathResource("meet_credentials.json").inputStream)
            .createScoped("https://www.googleapis.com/auth/meetings.space.created")

        val settings = SpacesServiceSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            .build()

        try {
            SpacesServiceClient.create(settings).use { spacesServiceClient ->
                val request =
                    CreateSpaceRequest.newBuilder()
                        .setSpace(Space.newBuilder().build())
                        .build()
                val space = spacesServiceClient.createSpace(request)
                System.out.printf("Space created: %s\n", space.meetingUri)
                return space
            }
        } catch (e: IOException) {
            // TODO(developer): Handle errors
            e.printStackTrace()
        }
        return null
    }
}
