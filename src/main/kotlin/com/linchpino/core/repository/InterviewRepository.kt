package com.linchpino.core.repository

import com.linchpino.core.entity.Interview
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InterviewRepository : JpaRepository<Interview, Long>