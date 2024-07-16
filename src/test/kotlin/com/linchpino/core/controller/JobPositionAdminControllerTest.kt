package com.linchpino.core.controller

import com.linchpino.core.dto.JobPositionCreateRequest
import com.linchpino.core.dto.JobPositionSearchResponse
import com.linchpino.core.service.JobPositionService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class JobPositionAdminControllerTest {
    @InjectMocks
    private lateinit var jobPositionAdminController: JobPositionAdminController

    @Mock
    private lateinit var jobPositionService: JobPositionService


    @Test
    fun `test deleteJobPosition`() {
        // Given
        val idToDelete = 1L

        // When
        jobPositionAdminController.deleteJobPosition(idToDelete)

        // Then
        verify(jobPositionService, times(1)).deleteById(idToDelete)
    }

    @Test
    fun `test updateJobPosition`() {
        // Given
        val idToUpdate = 1L
        val request = JobPositionCreateRequest("Updated Title")

        // When
        jobPositionAdminController.updateJobPosition(idToUpdate, request)

        // Then
        verify(jobPositionService, times(1)).update(idToUpdate, request)

    }

    @Test
    fun `test getJobPosition`() {
        // Given
        val idToFetch = 1L
        val expectedResponse = JobPositionSearchResponse(idToFetch, "Test Title")

        given(jobPositionService.getById(idToFetch)).willReturn(expectedResponse)

        // When
        val result = jobPositionAdminController.getJobPosition(idToFetch)

        // Then
        verify(jobPositionService, times(1)).getById(idToFetch)
        assertThat(result).isEqualTo(expectedResponse)
    }


    @Test
    fun `test addJobPosition calls service with provided arguments`() {
        val request = JobPositionCreateRequest("Mock Interview")

        jobPositionAdminController.addJobPosition(request)

        verify(jobPositionService, times(1)).createJobPosition(request)
    }
}
