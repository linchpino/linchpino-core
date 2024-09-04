package com.linchpino.core.service

import com.linchpino.core.captureNonNullable
import com.linchpino.core.dto.InterviewTypeCreateRequest
import com.linchpino.core.dto.InterviewTypeSearchResponse
import com.linchpino.core.dto.InterviewTypeUpdateRequest
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.entity.JobPosition
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.repository.findReferenceById
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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

    @Mock
    private lateinit var jobPositionRepository: JobPositionRepository


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
    fun `test search returns page of interviewTypes`() {
        // Given
        val page = PageImpl(mutableListOf(InterviewTypeSearchResponse(1, "InterviewType")))
        `when`(repository.search("interviewTitle", Pageable.ofSize(10))).thenReturn(page)

        // When
        val result = service.searchByName("interviewTitle", Pageable.ofSize(10))

        // Then
        assertThat(result).isEqualTo(page)

    }

    @Test
    fun `test create interviewType calls repository with provided arguments`() {
        // Given
        val interviewTypeCaptor: ArgumentCaptor<InterviewType> = ArgumentCaptor.forClass(InterviewType::class.java)
        val request = InterviewTypeCreateRequest("Mock Interview", 1)
        val jobPosition = JobPosition().apply {
            id = 1
        }

        `when`(jobPositionRepository.findReferenceById(1)).thenReturn(jobPosition)

        // When
        val result = service.createInterviewType(request)

        // Then
        verify(repository, times(1)).save(interviewTypeCaptor.captureNonNullable())

        val interviewType = interviewTypeCaptor.value
        assertThat(interviewType.name).isEqualTo(request.name)
        assertThat(interviewType.jobPositions.size).isEqualTo(1)
        assertThat(interviewType.jobPositions.first()).isEqualTo(jobPosition)
        assertThat(result.title).isEqualTo(request.name)
    }

    @Test
    fun `test get interview type by id returns interview type`() {
        // Given
        val expected = InterviewType().apply {
            id = 1
            name = "Mock Interview"
        }
        `when`(repository.findById(1)).thenReturn(Optional.of(expected))

        // When
        val result = service.getInterviewTypeById(1)

        // Then
        assertThat(result).isEqualTo(InterviewTypeSearchResponse(expected.id, expected.name))
        verify(repository, times(1)).findById(1)
    }

    @Test
    fun `test get interview type by id returns 404 if entity not found`() {
        // Given
        `when`(repository.findById(1)).thenReturn(Optional.empty())

        // When
        val ex = assertThrows<LinchpinException> {
            service.getInterviewTypeById(1)
        }

        // Then
        assertThat(ex.errorCode).isEqualTo(ErrorCode.ENTITY_NOT_FOUND)
    }


    @Test
    fun `test update interview type`() {
        // Given
        val interviewType = InterviewType().apply {
            id = 1
            name = "Mock Interview"
        }
        `when`(repository.findById(1)).thenReturn(Optional.of(interviewType))

        // When
        service.updateInterviewType(1, InterviewTypeUpdateRequest("newName"))

        // Then
        verify(repository, times(1)).findById(1)
        assertThat(interviewType.name).isEqualTo("newName")
    }

    @Test
    fun `test update interview type return 404 if entity not found`() {
        // Given
        `when`(repository.findById(1)).thenReturn(Optional.empty())

        // When
        val ex = assertThrows<LinchpinException> {
            service.updateInterviewType(1, InterviewTypeUpdateRequest("newName"))
        }

        // Then
        assertThat(ex.errorCode).isEqualTo(ErrorCode.ENTITY_NOT_FOUND)
    }

    @Test
    fun `test delete interview type`() {
        service.deleteInterviewType(1)
        verify(repository, times(1)).deleteById(1)
    }

}
