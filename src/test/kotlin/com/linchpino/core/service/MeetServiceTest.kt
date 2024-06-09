package com.linchpino.core.service

import com.google.api.core.ApiFuture
import com.google.api.gax.rpc.ApiCallContext
import com.google.api.gax.rpc.UnaryCallable
import com.google.apps.meet.v2.CreateSpaceRequest
import com.google.apps.meet.v2.Space
import com.google.apps.meet.v2.SpacesServiceClient
import com.google.apps.meet.v2.stub.SpacesServiceStub
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

@ExtendWith(MockitoExtension::class)
class MeetServiceTest {

    @Mock
    private lateinit var spaceServiceProvider: SpaceServiceProvider

    @InjectMocks
    private lateinit var service: MeetService


    @Test
    fun `test client calls createSpace method`() {
        // Given
        val client = FakeClient()
        `when`(spaceServiceProvider.spaceServiceClient()).thenReturn(client)

        // When
        val meetCode = service.createGoogleWorkSpace()

        // Then
        assertThat(meetCode).isEqualTo("fake-meet-code")
    }

}

private class FakeClient : SpacesServiceClient(FakeStub.withFakeSpace()) {
}

private class FakeStub private constructor(private val space: Space) : SpacesServiceStub() {

    companion object {
        fun withFakeSpace(): FakeStub {
            return FakeStub(Space.newBuilder().setMeetingCode("fake-meet-code").build())
        }
    }

    override fun createSpaceCallable(): UnaryCallable<CreateSpaceRequest, Space> {
        return object : UnaryCallable<CreateSpaceRequest, Space>() {
            override fun futureCall(p0: CreateSpaceRequest?, p1: ApiCallContext?): ApiFuture<Space> {
                return object : ApiFuture<Space> {
                    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
                        return false
                    }

                    override fun isCancelled() = false

                    override fun isDone() = false

                    override fun get(): Space {
                        return space
                    }

                    override fun get(timeout: Long, unit: TimeUnit): Space {
                        return space
                    }

                    override fun addListener(p0: Runnable?, p1: Executor?) {

                    }

                }
            }
        }
    }

    override fun close() {
    }

    override fun shutdown() {
    }

    override fun isShutdown() = false

    override fun isTerminated() = false

    override fun shutdownNow() {
    }

    override fun awaitTermination(p0: Long, p1: TimeUnit?) = false

}
