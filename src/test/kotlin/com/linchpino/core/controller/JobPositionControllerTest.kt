import com.linchpino.core.controller.JobPositionController
import com.linchpino.core.repository.JobPositionSearchResponse
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
		val page:Page<JobPositionSearchResponse> = PageImpl(
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
}
