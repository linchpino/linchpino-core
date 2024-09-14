package com.linchpino.core.controller

import com.linchpino.core.dto.InterviewTypeCreateRequest
import com.linchpino.core.dto.InterviewTypeSearchResponse
import com.linchpino.core.dto.InterviewTypeUpdateRequest
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.service.InterviewTypeService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.dao.DataIntegrityViolationException

@ExtendWith(MockitoExtension::class)
class InterviewTypeAdminControllerTest {

    @Mock
    lateinit var service: InterviewTypeService

    @InjectMocks
    lateinit var controller: InterviewTypeAdminController

    @Test
    fun `test create calls service with provided arguments`() {
        val request = InterviewTypeCreateRequest("Mock Interview", 1)
        `when`(service.createInterviewType(request)).thenReturn(InterviewTypeSearchResponse(1, request.name))

        val result = controller.addInterviewType(request)

        verify(service, times(1)).createInterviewType(request)
        assertThat(result).isEqualTo(InterviewTypeSearchResponse(1, request.name))
    }

    @Test
    fun `test get interview type by id calls service with provided arguments`() {
        // Given
        val id = 1L
        `when`(service.getInterviewTypeById(id)).thenReturn(InterviewTypeSearchResponse(1, "title"))

        // When
        val result = controller.getInterviewType(id)

        // Then
        verify(service, times(1)).getInterviewTypeById(id)
        assertThat(result.title).isEqualTo("title")
    }

    @Test
    fun `test update interview type calls service with provided arguments`() {
        // Given
        val request = InterviewTypeUpdateRequest("newTitle")

        // When
        controller.updateInterviewType(1, request)

        // Then
        verify(service, times(1)).updateInterviewType(1, request)
    }

    @Test
    fun `test delete interview type calls service with provided id`() {
        // Given and When
        controller.deleteInterviewTypeById(1)

        // Then
        verify(service, times(1)).deleteInterviewType(1)
    }

    @Test
    fun `test delete interview types throws proper exception if constraint violation happens`(){

        `when`(service.deleteInterviewType(1)).thenThrow(DataIntegrityViolationException::class.java)

        val ex = Assertions.assertThrows(LinchpinException::class.java){
            controller.deleteInterviewTypeById(1)
        }

        assertThat(ex.errorCode).isEqualTo(ErrorCode.INTEGRITY_VIOLATION)
    }
}
