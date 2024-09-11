package com.linchpino.ai.service.model;

import com.linchpino.ai.model.Resume;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumeTest extends ResumeMockData {

    private final Resume resume = new Resume("email@test.com", getResumeLines());

    @Test
    void getFullText() {
        // test: get full text
        String fullText = resume.getFullText();
        assertTrue(fullText.contains("Mohammad Masoomi"));
        assertFalse(fullText.contains("Senior Data Scientist"));
    }

    @Test
    void getSummary() {
        // test: get summary
        String summary = resume.getSummary();
        assertThat(summary, containsStringIgnoringCase("I am a java developer. Besides, I am an easy-going person and have a sense of humor."));
        assertFalse(summary.contains("Mohammad Masoomi"));
    }

    @Test
    void getExperience() {
        // test: get experience
        String experience = resume.getExperience();
        assertThat(experience, containsStringIgnoringCase("Software Engineer"));
        assertThat(experience, containsStringIgnoringCase("Java Software Developer"));
        assertFalse(experience.contains("Mohammad Masoomi"));
    }
}
