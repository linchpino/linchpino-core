package com.linchpino.core.repository

import com.linchpino.core.entity.JobPosition
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JobPositionRepository : JpaRepository<JobPosition, Long>