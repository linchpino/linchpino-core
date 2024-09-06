package com.linchpino.core.controller

import com.linchpino.core.captureNonNullable
import com.linchpino.core.dto.InterviewTypeSearchResponse
import com.linchpino.core.service.InterviewTypeService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

@ExtendWith(MockitoExtension::class)
class InterviewTypeControllerTest{

    @InjectMocks
    private lateinit var controller:InterviewTypeController

    @Mock
    private lateinit var service:InterviewTypeService


    @Test
    fun `test search calls service with provided arguments`(){
        // Given
        val nameCaptor: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        val pageCaptor: ArgumentCaptor<Pageable> = ArgumentCaptor.forClass(Pageable::class.java)

        // When
        controller.interviewTypes("interviewTypeName",Pageable.ofSize(10))

        // Then

        verify(service, times(1)).searchByName(nameCaptor.capture(),pageCaptor.captureNonNullable())
        val page = pageCaptor.value
        val name = nameCaptor.value

        assertThat(name).isEqualTo("interviewTypeName")
        assertThat(page.pageSize).isEqualTo(10)
    }

    @Test
    fun `test search returns page of interviewTypes`(){
        // Given
        val page = PageImpl(mutableListOf(InterviewTypeSearchResponse(1,"InterviewType")))
        `when`(service.searchByName("interviewTitle", Pageable.ofSize(10))).thenReturn(page)

        // When
        val result = controller.interviewTypes("interviewTitle", Pageable.ofSize(10))

        // Then
        assertThat(result).isEqualTo(page)

    }
}
