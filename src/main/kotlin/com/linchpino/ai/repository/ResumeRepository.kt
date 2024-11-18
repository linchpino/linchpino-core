package com.linchpino.ai.repository

import com.linchpino.ai.model.Resume
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ResumeRepository : JpaRepository<Resume?, Long?> {
    fun countAllByEmail(email: String?): Int
}
