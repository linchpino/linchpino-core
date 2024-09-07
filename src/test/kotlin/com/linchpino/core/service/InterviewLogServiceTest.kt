package com.linchpino.core.service

import com.linchpino.core.captureNonNullable
import com.linchpino.core.entity.InterviewLog
import com.linchpino.core.enums.InterviewLogType
import com.linchpino.core.repository.InterviewLogRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class InterviewLogServiceTest{

    @InjectMocks
    private lateinit var service: InterviewLogService

    @Mock
    lateinit var interviewLogRepository: InterviewLogRepository


    @Test
    fun `test save logs`(){
        // Given
        val captor: ArgumentCaptor<InterviewLog> = ArgumentCaptor.forClass(InterviewLog::class.java)

        // When
        service.save(InterviewLogType.CREATED,1)

        // Then
        verify(interviewLogRepository, times(1)).save(captor.captureNonNullable())

        val interviewLog = captor.value
        assertThat(interviewLog.type).isEqualTo(InterviewLogType.CREATED)
        assertThat(interviewLog.createdBy).isEqualTo(1)

    }
}
