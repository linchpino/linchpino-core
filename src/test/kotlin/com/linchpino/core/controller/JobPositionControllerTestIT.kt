package com.linchpino.core.controller

import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.entity.JobPosition
import com.linchpino.core.repository.JobPositionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@Import(PostgresContainerConfig::class)
@Transactional
@SpringBootTest
class JobPositionControllerTestIT{


	@Autowired
	private lateinit var mockMvc: MockMvc


	@Autowired
	private lateinit var jobPositionRepository: JobPositionRepository

	@BeforeEach
	fun setUp() {
		val interviewType1 = InterviewType().apply { name = "InterViewTyp_1" }
		val interviewType2 = InterviewType().apply { name = "InterViewTyp_2" }
		val jobPositions = jobPositions()
		jobPositions.first { it.title == "Software Engineer" }.apply {
			interviewTypes.add(interviewType1)
			interviewTypes.add(interviewType2)
		}
		jobPositionRepository.saveAll(jobPositions)
	}

	@Test
	fun `test jobPositions search by name`() {

		mockMvc.perform(get("/api/jobposition/search?name=Engineer"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].title").value("Software Engineer"))
	}


	@Test
	fun `test jobPositions search by name returns empty result when jobPosition is not in the database`() {

		mockMvc.perform(get("/api/jobposition/search?name=Mechanic"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.content.length()").value(0))
	}

	@Test
	fun `test jobPositions search without providing name returns page of jobPositions`() {

		mockMvc.perform(get("/api/jobposition/search"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.content.length()").value(10))
			.andExpect(jsonPath("$.content[0].title").value("Software Engineer"))
			.andExpect(jsonPath("$.content[1].title").value("Data Scientist"))
			.andExpect(jsonPath("$.content[2].title").value("Product Manager"))
			.andExpect(jsonPath("$.content[3].title").value("Web Developer"))
			.andExpect(jsonPath("$.content[4].title").value("Marketing Specialist"))
			.andExpect(jsonPath("$.content[5].title").value("Human Resources Manager"))
			.andExpect(jsonPath("$.content[6].title").value("Financial Analyst"))
			.andExpect(jsonPath("$.content[7].title").value("Graphic Designer"))
			.andExpect(jsonPath("$.content[8].title").value("Customer Service Representative"))
			.andExpect(jsonPath("$.content[9].title").value("Project Coordinator"))
	}



	@Test
	fun `test jobPositions search without providing name returns page of jobPositions based on provided page size`() {

		mockMvc.perform(get("/api/jobposition/search?size=5"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.content.length()").value(5))
			.andExpect(jsonPath("$.content[0].title").value("Software Engineer"))
			.andExpect(jsonPath("$.content[1].title").value("Data Scientist"))
			.andExpect(jsonPath("$.content[2].title").value("Product Manager"))
			.andExpect(jsonPath("$.content[3].title").value("Web Developer"))
			.andExpect(jsonPath("$.content[4].title").value("Marketing Specialist"))
	}


	@Test
	fun `test interviewTypes endpoint`() {
		val jobId = 1L
		// Perform the GET request to the endpoint
		mockMvc.perform(get("/api/jobposition/$jobId/interviewtype"))
			.andExpect(status().isOk)
	}

	private fun jobPositions() = listOf(
		JobPosition().apply { 	title = "Software Engineer"	},
		JobPosition().apply { 	title = "Data Scientist"	},
		JobPosition().apply { 	title = "Product Manager"	},
		JobPosition().apply { 	title = "Web Developer"	},
		JobPosition().apply { 	title = "Marketing Specialist"	},
		JobPosition().apply { 	title = "Human Resources Manager"	},
		JobPosition().apply { 	title = "Financial Analyst"	},
		JobPosition().apply { 	title = "Graphic Designer"	},
		JobPosition().apply { 	title = "Customer Service Representative"	},
		JobPosition().apply { 	title = "Project Coordinator"	},
	)
}
