package com.linchpino.core.service

import com.google.apps.meet.v2.CreateSpaceRequest
import com.google.apps.meet.v2.Space
import org.springframework.stereotype.Service

@Service
class MeetService(
    private val spaceServiceProvider: SpaceServiceProvider
) {

    fun createGoogleWorkSpace(): String? {
        spaceServiceProvider.spaceServiceClient().use { client ->
            val request = CreateSpaceRequest.newBuilder()
                .setSpace(Space.newBuilder().build())
                .build()
            val space = client.createSpace(request)
            return space?.meetingCode
        }
    }
}
