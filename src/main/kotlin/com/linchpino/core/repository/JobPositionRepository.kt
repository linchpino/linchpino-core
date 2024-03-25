package com.linchpino.core.repository

import com.linchpino.core.dto.InterviewTypeSearchResponse
import com.linchpino.core.dto.JobPositionSearchResponse
import com.linchpino.core.entity.JobPosition
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface JobPositionRepository : JpaRepository<JobPosition, Long> {

    @Query(
        """
        SELECT new com.linchpino.core.dto.JobPositionSearchResponse(jp.id, jp.title)
        FROM JobPosition jp
        WHERE (:title IS NULL OR jp.title ILIKE %:title%)
    """
    )
    fun search(title: String?, pageable: Pageable): Page<JobPositionSearchResponse>

    @Query(
        """
		SELECT new com.linchpino.core.dto.InterviewTypeSearchResponse(it.id, it.name)
		FROM InterviewType it
		JOIN it.jobPositions jp
		WHERE jp.id = :id
	"""
    )
    fun findInterviewsByJobPositionId(id: Long, pageable: Pageable): Page<InterviewTypeSearchResponse>
}


