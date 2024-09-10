package com.linchpino.ai.repository;

import com.linchpino.ai.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    int countAllByEmail(String email);
}
