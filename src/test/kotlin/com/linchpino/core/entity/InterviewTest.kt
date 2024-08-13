package com.linchpino.core.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class InterviewTest {
    @Test
    fun `interviewPartiesFullName should return full names when both names are present`() {
        // Given
        val jobSeeker = Account().apply {
            //("John", "Doe", "john.doe@example.com")
            firstName = "John"
            lastName = "Doe"
            email = "john.doe@example.com"
        }
        val mentor = Account().apply {
            //("Jane", "Smith", "jane.smith@example.com")
            firstName = "Jane"
            lastName = "Smith"
            email = "jane.smith@example.com"
        }
        val interview = Interview().apply {
            mentorAccount = mentor
            jobSeekerAccount = jobSeeker
        }

        // When
        val result = interview.interviewPartiesFullName()

        // Then
        assertThat(result).isEqualTo("Jane Smith" to "John Doe")
    }

    @Test
    fun `interviewPartiesFullName should return default name when job seeker first name is null`() {
        // Given
        val jobSeeker = Account().apply {
            lastName = "Doe"
            email = "john.doe@example.com"
        }
        val mentor = Account().apply {
            firstName = "Jane"
            lastName = "Smith"
            email = "jane.smith@example.com"
        }
        val interview = Interview().apply {
            mentorAccount = mentor
            jobSeekerAccount = jobSeeker
        }

        // When
        val result = interview.interviewPartiesFullName()

        // Then
        assertThat(result).isEqualTo("Jane Smith" to "JobSeeker")
    }

    @Test
    fun `interviewPartiesFullName should return default name when job seeker last name is null`() {
        // Given
        val jobSeeker = Account().apply {
            firstName = "John"
            email = "john.doe@example.com"
        }
        val mentor = Account().apply {
            firstName = "Jane"
            lastName = "Smith"
            email = "jane.smith@example.com"
        }
        val interview = Interview().apply {
            mentorAccount = mentor
            jobSeekerAccount = jobSeeker
        }

        // When
        val result = interview.interviewPartiesFullName()

        // Then
        assertThat(result).isEqualTo("Jane Smith" to "JobSeeker")
    }

    @Test
    fun `interviewPartiesFullName should return default name when mentor first name is null`() {
        // Given
        val jobSeeker = Account().apply {
            firstName = "John"
            lastName = "Doe"
            email = "john.doe@example.com"
        }
        val mentor = Account().apply {
            lastName = "Smith"
            email = "jane.smith@example.com"
        }
        val interview = Interview().apply {
            mentorAccount = mentor
            jobSeekerAccount = jobSeeker
        }

        // When
        val result = interview.interviewPartiesFullName()

        // Then
        assertThat(result).isEqualTo("Mentor" to "John Doe")
    }

    //
    @Test
    fun `interviewPartiesFullName should return default name when mentor last name is null`() {
        // Given
        val jobSeeker = Account().apply {
            firstName = "John"
            lastName = "Doe"
            email = "john.doe@example.com"
        }
        val mentor = Account().apply {
            firstName = "Jane"
            email = "jane.smith@example.com"
        }
        val interview = Interview().apply {
            mentorAccount = mentor
            jobSeekerAccount = jobSeeker
        }

        // When
        val result = interview.interviewPartiesFullName()

        // Then
        assertThat(result).isEqualTo("Mentor" to "John Doe")
    }

    //
    @Test
    fun `interviewPartiesFullName should return default names when both first and last names are null`() {
        // Given
        val jobSeeker = Account().apply {
            email = "john.doe@example.com"
        }
        val mentor = Account().apply {
            email = "jane.smith@example.com"
        }
        val interview = Interview().apply {
            mentorAccount = mentor
            jobSeekerAccount = jobSeeker
        }

        // When
        val result = interview.interviewPartiesFullName()

        // Then
        assertThat(result).isEqualTo("Mentor" to "JobSeeker")
    }
}
