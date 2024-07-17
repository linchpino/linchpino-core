package com.linchpino.core.controller

import com.linchpino.core.dto.InterviewTypeCreateRequest
import com.linchpino.core.dto.InterviewTypeSearchResponse
import com.linchpino.core.service.InterviewTypeService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class InterviewTypeAdminControllerTest {

    @Mock
    lateinit var service: InterviewTypeService

    @InjectMocks
    lateinit var controller: InterviewTypeAdminController

    @Test
    fun `test create calls service with provided arguments`(){
        val request = InterviewTypeCreateRequest("Mock Interview", 1)
        `when`(service.createInterviewType(request)).thenReturn(InterviewTypeSearchResponse(1,request.name))

        val result = controller.addInterviewType(request)

        verify(service, times(1)).createInterviewType(request)
        assertThat(result).isEqualTo(InterviewTypeSearchResponse(1,request.name))
    }
}
