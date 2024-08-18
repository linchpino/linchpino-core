package com.linchpino.ai.service.domain;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumePDFTest {

    private final ResumePDF resumePDF = new ResumePDF("ai/pdf/linkedin-profile.pdf");

    @Test
    void getFullText() {
        // sanity check
        assertThrows(IllegalArgumentException.class, () -> new ResumePDF(""));
        // test: get full text
        String fullText = resumePDF.getFullText();
        assertTrue(fullText.contains("Mohammad Masoomi"));
        assertFalse(fullText.contains("Senior Data Scientist"));
    }

    @Test
    void getSummary() {
        // test: get summary
        String summary = resumePDF.getSummary();
        assertThat(summary, containsStringIgnoringCase("I am a java developer. Besides, I am an easy-going person and have a sense of humor."));
        assertFalse(summary.contains("Mohammad Masoomi"));
    }

    @Test
    void getExperience() {
        // test: get experience
        String experience = resumePDF.getExperience();
        assertThat(experience, containsStringIgnoringCase("Software Engineer"));
        assertThat(experience, containsStringIgnoringCase("Java Software Developer"));
        assertFalse(experience.contains("Mohammad Masoomi"));
    }
}
