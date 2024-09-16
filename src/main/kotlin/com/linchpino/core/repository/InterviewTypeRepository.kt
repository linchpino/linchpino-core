package com.linchpino.core.repository

import com.linchpino.core.dto.InterviewTypeSearchResponse
import com.linchpino.core.entity.InterviewType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface InterviewTypeRepository : JpaRepository<InterviewType, Long> {

    fun findAllByIdIn(ids:List<Long>):List<InterviewType>

    @Query(
        """
        SELECT it FROM InterviewType it join fetch it.jobPositions jp
        WHERE (:name IS NULL OR it.name ILIKE %:name%)
    """
    )
    fun search(name: String?, pageable: Pageable): Page<InterviewType>

}
