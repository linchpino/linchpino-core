package com.linchpino.core.service

import com.linchpino.core.NonNullableArgumentCaptor
import com.linchpino.core.repository.InterviewTypeSearchResponse
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.repository.JobPositionSearchResponse
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

	@Captor
	private lateinit var nameCaptor: ArgumentCaptor<String?>

	@Captor
	private lateinit var pageableCaptor: ArgumentCaptor<Pageable>

	@Test
	fun `test searchByName`() {
		// Given
		val page: Page<JobPositionSearchResponse> = PageImpl(emptyList())

		`when`(jobPositionRepository.search(nameCaptor.capture(), NonNullableArgumentCaptor.capture(pageableCaptor))).thenReturn(page)

		// When
		val result: Page<JobPositionSearchResponse> = jobPositionService.searchByName("Software Engineer", Pageable.unpaged())

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
		`when`(jobPositionRepository.findInterviewsByJobPositionId(jobPositionId)).thenReturn(listOf(interviewType1))

		// When
		val result = jobPositionService.findInterviewTypesFor(jobPositionId)

		// Then
		verify(jobPositionRepository, times(1)).findInterviewsByJobPositionId(idCaptor.capture())
		assertThat(idCaptor.value).isEqualTo(jobPositionId)
		assertThat(result.size).isEqualTo(1)
		assertThat(result.first()).isEqualTo(interviewType1)
	}

}
