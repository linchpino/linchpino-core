package com.linchpino.demo.repository

import com.linchpino.demo.entity.Interview
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InterviewRepository : JpaRepository<Interview, Long>