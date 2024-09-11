package com.linchpino.ai.service.impl;

import com.linchpino.ai.model.Resume;
import com.linchpino.ai.repository.ResumeRepository;
import com.linchpino.core.exception.ErrorCode;
import com.linchpino.core.exception.LinchpinException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class ResumeService {

    private final Logger logger = LoggerFactory.getLogger(ResumeService.class);

    @Value("${spring.ai.roadmap-attempt-limit}")
    private int roadmapAttemptLimit;

    private final ResumeRepository resumeRepository;

    public ResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    public Resume create(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        List<String> lines = readLines(file);
        String email = Resume.findEmail(lines);
        if (isRoadmapAttemptLimitReached(email)) {
            throw new LinchpinException(ErrorCode.TOO_MANY_ATTEMPT, "Roadmap attempt limit reached for email: {}", email);
        }
        return new Resume(email, lines);
    }

    public Resume save(Resume resume) {
        Objects.requireNonNull(resume, "Resume cannot be null");
        Objects.requireNonNull(resume.getEmail(), "Email cannot be null");
        return resumeRepository.save(resume);
    }

    public Resume findById(Long id) {
        return resumeRepository.findById(id).orElse(null);
    }

    public List<Resume> findAll() {
        return resumeRepository.findAll();
    }

    private boolean isRoadmapAttemptLimitReached(String email) {
        return resumeRepository.countAllByEmail(email) >= roadmapAttemptLimit;
    }

    /**
     * Read the lines from the PDF file
     *
     * @param file File
     * @return List of lines
     */
    private List<String> readLines(File file) {
        try (PDDocument document = Loader.loadPDF(file)) {
            logger.info("Reading the file: {}", file.getName());
            PDFTextStripper textStripper = new PDFTextStripper();
            return new ArrayList<>(Arrays.asList(textStripper.getText(document).split("\n")));
        } catch (IOException e) {
            throw new LinchpinException(ErrorCode.SERVER_ERROR, "Error occurred while reading the file!", e);
        }
    }

}
