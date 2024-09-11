package com.linchpino.ai.model;

import java.util.Arrays;
import java.util.stream.Stream;

public enum ResumeHeader {
    NAME("Name", "Name", "Full Name", "First Name", "Last Name"),
    EMAIL("Email", "Email Address", "Email", "E-mail"),
    PHONE("Phone", "Phone", "Phone Number", "Telephone", "Mobile"),
    ADDRESS("Address", "Address", "Location", "City", "State", "Zip", "Postal Code"),
    LINKEDIN("LinkedIn", "LinkedIn", "LinkedIn Profile", "LinkedIn URL"),
    GITHUB("GitHub", "GitHub Profile", "GitHub"),
    SUMMARY("Summary", "About", "Summary", "About me", "Professional Summary", "Objective", "Career Summary", "Profile"),
    EXPERIENCE("Experience", "Experience", "Work Experience", "Professional Experience", "Employment", "Work History"),
    EDUCATION("Education", "Education", "Academic Background", "Academic Qualifications", "Educational Qualifications", "Academic History", "Educational Background", "Educational History", "Academic Experience", "Educational Experience"),
    SKILLS("Skills", "Skills", "Top Skills", "Key Skills", "Technical Skills", "Core Competencies", "Professional Skills", "Soft Skills", "Hard Skills", "Technical Proficiencies"),
    PROJECTS("Projects", "Projects", "Project Experience", "Project History", "Project Work", "Project Details", "Project Portfolio", "Project List"),
    CERTIFICATIONS("Certifications", "Certifications", "Certification", "Certified", "Certification & Training", "Certification & Licenses", "Certification & Awards", "Certification & Achievements", "Certification & Professional Development"),
    COURSES("Courses", "Courses", "Training", "Professional Development", "Professional Training", "Professional Courses", "Professional Development Courses", "Professional Training Courses"),
    LANGUAGES("Languages", "Languages", "Language Proficiency", "Language Skills", "Language Abilities", "Language Proficiencies", "Language Abilities", "Language Skills & Proficiencies"),
    INTERESTS("Interests", "Interests", "Hobbies", "Activities", "Personal Interests", "Personal Activities", "Personal Hobbies"),
    ;

    private final String label;
    private final String[] headers;

    ResumeHeader(String label, String... headers) {
        this.label = label;
        this.headers = headers;
    }

    public static String[] getContact() {
        return Stream.of(NAME, EMAIL, PHONE, ADDRESS)
            .map(ResumeHeader::getHeaders)
            .flatMap(Arrays::stream)
            .toArray(String[]::new);
    }

    public String getLabel() {
        return label;
    }

    public String[] getHeaders() {
        return headers;
    }

    public static String[] getAll() {
        return Stream.of(ResumeHeader.values())
            .map(ResumeHeader::getHeaders)
            .flatMap(Arrays::stream)
            .toArray(String[]::new);
    }

    public static String[] getExperience() {
        return EXPERIENCE.headers;
    }

    public static String[] getSummary() {
        return SUMMARY.headers;
    }
}
