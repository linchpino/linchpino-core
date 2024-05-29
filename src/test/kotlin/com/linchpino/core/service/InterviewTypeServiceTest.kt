package com.linchpino.core.service

import com.linchpino.core.captureNonNullable
import com.linchpino.core.dto.InterviewTypeSearchResponse
import com.linchpino.core.repository.InterviewTypeRepository
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
class InterviewTypeServiceTest {

    @InjectMocks
    private lateinit var service: InterviewTypeService

    @Mock
    private lateinit var repository: InterviewTypeRepository


    @Test
    fun `test search calls repository with provided arguments`() {
        // Given
        val nameCaptor: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        val pageCaptor: ArgumentCaptor<Pageable> = ArgumentCaptor.forClass(Pageable::class.java)

        // When
        service.searchByName("interviewTitle", Pageable.ofSize(10))

        // Then
        verify(repository, times(1)).search(nameCaptor.capture(), pageCaptor.captureNonNullable())
        val page = pageCaptor.value
        val name = nameCaptor.value

        assertThat(name).isEqualTo("interviewTitle")
        assertThat(page.pageSize).isEqualTo(10)
    }

    @Test
    fun `test search returns page of interviewTypes`(){
        // Given
        val page = PageImpl(mutableListOf(InterviewTypeSearchResponse(1,"InterviewType")))
        `when`(repository.search("interviewTitle",Pageable.ofSize(10))).thenReturn(page)

        // When
        val result = service.searchByName("interviewTitle", Pageable.ofSize(10))

        // Then
        assertThat(result).isEqualTo(page)

    }

}
