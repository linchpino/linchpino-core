package com.linchpino.demo.repository

import com.linchpino.demo.entity.JobPosition
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JobPositionRepository : JpaRepository<JobPosition, Long>