package com.linchpino.core.service

import com.linchpino.core.PostgresContainerConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(PostgresContainerConfig::class)
class MeetServiceTestIT {

    @Autowired
    lateinit var service: MeetService

    @Test
    fun `test space`(){
        val space = service.createGoogleWorkSpace()
        assertThat(space).isNotNull()
        println(space?.meetingUri)
    }
}
