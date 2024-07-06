package com.linchpino.core.controller

import com.linchpino.core.dto.InterviewTypeSearchResponse
import com.linchpino.core.dto.JobPositionCreateRequest
import com.linchpino.core.dto.JobPositionSearchResponse
import com.linchpino.core.service.JobPositionService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

@ExtendWith(MockitoExtension::class)
class JobPositionControllerTest {

    @InjectMocks
    private lateinit var jobPositionController: JobPositionController

    @Mock
    private lateinit var jobPositionService: JobPositionService


    @Test
    fun `test jobPositions passes correct params to service layer`() {
        // Given
        val page = PageImpl(emptyList<JobPositionSearchResponse>())

        `when`(jobPositionService.searchByName("something", Pageable.unpaged())).thenReturn(page)

        // When
        jobPositionController.jobPositions("something", Pageable.unpaged())

        // Then
        verify(jobPositionService, times(1)).searchByName("something", Pageable.unpaged())
    }

    @Test
    fun `test jobPositions with name`() {
        // Given
        val page: Page<JobPositionSearchResponse> = PageImpl(
            listOf(
                JobPositionSearchResponse(1, "Software Engineer")
            )
        )

        val name = "Software Engineer"

        `when`(
            jobPositionService.searchByName(
                name,
                Pageable.unpaged()
            )
        ).thenReturn(page)


        // When
        val result = jobPositionController.jobPositions(name, Pageable.unpaged())

        // Then
        verify(jobPositionService, times(1)).searchByName(name, Pageable.unpaged())
        assertThat(result.totalPages).isEqualTo(1)
        assertThat(result.content.size).isEqualTo(1)

    }

    @Test
    fun `test find interviewTypes by jobPositionId`() {
        // Given
        val page: Page<InterviewTypeSearchResponse> = PageImpl(
            listOf(
                InterviewTypeSearchResponse(1, "InterviewType1")
            )
        )

        `when`(
            jobPositionService.findInterviewTypesBy(
                1,
                Pageable.unpaged()
            )
        ).thenReturn(page)


        // When
        val result = jobPositionController.interviewTypes(1, Pageable.unpaged())

        // Then
        verify(jobPositionService, times(1)).findInterviewTypesBy(1, Pageable.unpaged())
        assertThat(result.totalPages).isEqualTo(1)
        assertThat(result.content.size).isEqualTo(1)
        assertThat(result.content[0].title).isEqualTo("InterviewType1")

    }

    @Test
    fun `test addJobPosition calls service with provided arguments`() {
        val request = JobPositionCreateRequest("Mock Interview", 1)

        jobPositionController.addJobPosition(request)

        verify(jobPositionService, times(1)).createJobPosition(request)
    }
}
