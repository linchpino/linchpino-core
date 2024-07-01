package com.linchpino.core.service

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.ConferenceData
import com.google.api.services.calendar.model.ConferenceSolutionKey
import com.google.api.services.calendar.model.CreateConferenceRequest
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventAttendee
import com.google.api.services.calendar.model.EventDateTime
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.time.ZonedDateTime
import java.util.Date
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

@Service
class CalendarService(
    @Value("\${meet.credential}") private val credential: Resource,
    private var calendar: Calendar? = null
) {

    private fun calendar(): Calendar {
        val requestInitializer = HttpCredentialsAdapter(credentials())

        val calendar = Calendar.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            requestInitializer
        ).setApplicationName("Linchpino")
            .build()
        this.calendar = calendar
        return calendar
    }

    private fun credentials(): GoogleCredentials? {
        val credentials = GoogleCredentials.fromStream(credential.inputStream)
            .createScoped(listOf("https://www.googleapis.com/auth/calendar"))
            .createDelegated("linchpino@linchpino.com")
        return credentials
    }

    fun googleMeetCode(attendees: List<String>, eventTitle: String, times: Pair<ZonedDateTime, ZonedDateTime>): String {

        var event = Event()
            .setSummary(eventTitle)
            .setStart(EventDateTime().setDateTime(DateTime(Date.from(times.first.toInstant()))).setTimeZone("UTC"))
            .setEnd(EventDateTime().setDateTime(DateTime(Date.from(times.second.toInstant()))).setTimeZone("UTC"))


        event.attendees = attendees.map { mail ->
            EventAttendee().apply {
                email = mail
            }
        }

        event.conferenceData = ConferenceData().setCreateRequest(
            CreateConferenceRequest().apply {
                requestId = UUID.randomUUID().toString()
                conferenceSolutionKey = ConferenceSolutionKey().apply {
                    type = "hangoutsMeet"
                }
            }
        )
        event = (calendar ?: calendar())
            .events()
            .insert("primary", event)
            .setSendUpdates("all")
            .setConferenceDataVersion(1)
            .execute()
        return event.conferenceData.conferenceId
    }

}
