package com.linchpino.core.service

import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.ConferenceData
import com.google.api.services.calendar.model.Event
import java.time.ZonedDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.core.io.Resource

class CalendarServiceTest {

    @Mock
    private lateinit var calendar: Calendar
    @Mock
    private lateinit var resource: Resource

    private lateinit var calendarService: CalendarService

    @Captor
    private lateinit var eventCaptor: ArgumentCaptor<Event>

    @Mock
    private lateinit var events: Calendar.Events
    @Mock
    private lateinit var insert: Calendar.Events.Insert

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        calendarService = CalendarService(resource,calendar)
    }

    @Test
    fun `test googleMeetCode`() {
        // Given
        val attendees = listOf("attendee1@example.com", "attendee2@example.com")
        val eventTitle = "Test Event"
        val startTime = ZonedDateTime.now()
        val endTime = ZonedDateTime.now().plusHours(1)


        val conferenceId = "mocked-conference-id"
        `when`(calendar.events()).thenReturn(events)
        `when`(events.insert(anyString(), any(Event::class.java))).thenReturn(insert)
        `when`(insert.setSendUpdates(anyString())).thenReturn(insert)
        `when`(insert.setConferenceDataVersion(anyInt())).thenReturn(insert)
        `when`(insert.execute()).thenReturn(Event().setConferenceData(ConferenceData().setConferenceId(conferenceId)))

        // When
        val resultConferenceId = calendarService.googleMeetCode(attendees, eventTitle, startTime to endTime)

        // Then
        verify(calendar.events()).insert(eq("primary"), eventCaptor.capture())
        val capturedEvent = eventCaptor.value

        assertThat(resultConferenceId).isEqualTo(conferenceId)
        assertThat(capturedEvent.summary).isEqualTo(eventTitle)
        assertThat(capturedEvent.attendees.map { it.email }).isEqualTo(attendees)
    }
}
