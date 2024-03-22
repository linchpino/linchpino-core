package com.linchpino.core.repository

import com.linchpino.core.entity.JobPosition
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

data class JobPositionSearchResponse(val id:Long,val title: String)
@Repository
interface JobPositionRepository : JpaRepository<JobPosition, Long> {

	@Query("""
        SELECT new com.linchpino.core.repository.JobPositionSearchResponse(jp.id, jp.title)
        FROM JobPosition jp
        WHERE (:title IS NULL OR jp.title LIKE %:title%)
    """)
	fun search(title:String?,pageable: Pageable): Page<JobPositionSearchResponse>
}


