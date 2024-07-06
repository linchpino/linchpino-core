package com.linchpino.core.service

import com.linchpino.core.captureNonNullable
import com.linchpino.core.dto.InterviewTypeSearchResponse
import com.linchpino.core.dto.JobPositionCreateRequest
import com.linchpino.core.dto.JobPositionSearchResponse
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.entity.JobPosition
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.repository.findReferenceById
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
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
class JobPositionServiceTest {

    @InjectMocks
    private lateinit var jobPositionService: JobPositionService

    @Mock
    private lateinit var jobPositionRepository: JobPositionRepository

    @Mock
    private lateinit var interviewTypeRepository: InterviewTypeRepository

    @Captor
    private lateinit var nameCaptor: ArgumentCaptor<String?>

    @Captor
    private lateinit var pageableCaptor: ArgumentCaptor<Pageable>

    @Test
    fun `test searchByName`() {
        // Given
        val page: Page<JobPositionSearchResponse> = PageImpl(emptyList())

        `when`(
            jobPositionRepository.search(
                nameCaptor.capture(),
                pageableCaptor.captureNonNullable()
            )
        ).thenReturn(page)

        // When
        val result: Page<JobPositionSearchResponse> =
            jobPositionService.searchByName("Software Engineer", Pageable.unpaged())

        // Then
        assertThat(nameCaptor.value).isEqualTo("Software Engineer")
        assertThat(pageableCaptor.value).isEqualTo(Pageable.unpaged())
        assertThat(result).isEqualTo(page)
        verify(jobPositionRepository, times(1)).search("Software Engineer", Pageable.unpaged())

    }

    @Test
    fun `test find interview types based on job position id`() {
        // Given
        val jobPositionId = 123L

        val idCaptor = ArgumentCaptor.forClass(Long::class.java)
        val interviewType1 = InterviewTypeSearchResponse(1, "InterviewType1")

        `when`(jobPositionRepository.findInterviewsByJobPositionId(jobPositionId, Pageable.unpaged())).thenReturn(
            PageImpl(listOf(interviewType1))
        )

        // When
        val result = jobPositionService.findInterviewTypesBy(jobPositionId, Pageable.unpaged())

        // Then
        verify(jobPositionRepository, times(1)).findInterviewsByJobPositionId(
            idCaptor.capture(),
            pageableCaptor.captureNonNullable()
        )
        assertThat(idCaptor.value).isEqualTo(jobPositionId)
        assertThat(result.content.size).isEqualTo(1)
        assertThat(result.totalPages).isEqualTo(1)
        assertThat(result.first()).isEqualTo(interviewType1)
    }

    @Test
    fun `test find interview types returns empty list if no interviewType found for that job id`() {
        // Given
        val jobPositionId = 123L

        val idCaptor = ArgumentCaptor.forClass(Long::class.java)

        `when`(jobPositionRepository.findInterviewsByJobPositionId(jobPositionId, Pageable.unpaged())).thenReturn(
            PageImpl(listOf())
        )

        // When
        val result = jobPositionService.findInterviewTypesBy(jobPositionId, Pageable.unpaged())

        // Then
        verify(jobPositionRepository, times(1)).findInterviewsByJobPositionId(
            idCaptor.capture(),
            pageableCaptor.captureNonNullable()
        )
        assertThat(idCaptor.value).isEqualTo(jobPositionId)
        assertThat(result.content.size).isEqualTo(0)
    }

    @Test
    fun `test create jobPosition`() {
        // Given
        val jobPositionCaptor: ArgumentCaptor<JobPosition> = ArgumentCaptor.forClass(JobPosition::class.java)
        val request = JobPositionCreateRequest("Java Developer", 1)
        val interviewType = InterviewType().apply {
            id = 1
        }

        `when`(interviewTypeRepository.findReferenceById(1)).thenReturn(interviewType)

        // When
        jobPositionService.createJobPosition(request)

        // Then
        verify(jobPositionRepository, times(1)).save(jobPositionCaptor.captureNonNullable())

        val jobPosition = jobPositionCaptor.value
        assertThat(jobPosition.title).isEqualTo(request.title)
        assertThat(jobPosition.interviewTypes().size).isEqualTo(1)
        assertThat(jobPosition.interviewTypes().first()).isEqualTo(interviewType)
    }

}
