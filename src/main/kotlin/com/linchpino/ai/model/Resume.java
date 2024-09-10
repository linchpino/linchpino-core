package com.linchpino.ai.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Entity
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    @ElementCollection(targetClass = String.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "resume_lines", joinColumns = @JoinColumn(name = "resume_id"))
    @Column(name = "line", nullable = false)
    private List<String> lines = new ArrayList<>();

    public Resume(String email, List<String> lines) {
        this.email = email;
        this.lines = lines;
    }

    public Resume() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    public static String findEmail(List<String> lines) {
        return Contact.findEmail(lines);
    }

    public String getFullText() {
        return String.join(" ", lines);
    }

    public String getSummary() {
        return parse(lines, new Summary()).toString();
    }

    public String getExperience() {
        return parse(lines, new Experience()).toString();
    }

    private Section parse(List<String> textLines, Section section) {
        for (int lineCntr = 0; lineCntr < textLines.size(); lineCntr++) {
            String header = lineIsHeader(textLines.get(lineCntr), section.getHeaders());
            if (header == null) {
                continue;
            }
            for (int restLineCntr = lineCntr + 1; restLineCntr < textLines.size(); restLineCntr++) {
                String restLine = textLines.get(restLineCntr);
                if (lineIsHeader(restLine, ResumeHeader.getAll()) != null) {
                    break;
                }
                section.addLine(restLine);
            }
        }
        return section;
    }

    private String lineIsHeader(String line, String[] headers) {
        for (String header : headers) {
            if (line.toLowerCase().contains(header.toLowerCase()) &&
                    line.length() < 2 * header.length()) {
                return header;
            }
        }
        return null;
    }

    private interface Section {
        String[] getHeaders();

        void addLine(String line);
    }

    private static class Summary implements Section {
        private List<String> summaryLines = new ArrayList<>();

        public List<String> getSummaryLines() {
            return summaryLines;
        }

        public void setSummaryLines(List<String> summaryLines) {
            this.summaryLines = summaryLines;
        }

        @Override
        public String[] getHeaders() {
            return ResumeHeader.getSummary();
        }

        @Override
        public void addLine(String line) {
            summaryLines.add(line);
        }

        @Override
        public String toString() {
            return String.join(" ", summaryLines);
        }
    }

    private static class Experience implements Section {
        private List<String> experienceLines = new ArrayList<>();

        public List<String> getExperienceLines() {
            return experienceLines;
        }

        public void setExperienceLines(List<String> experienceLines) {
            this.experienceLines = experienceLines;
        }

        @Override
        public String[] getHeaders() {
            return ResumeHeader.getExperience();
        }

        @Override
        public void addLine(String line) {
            experienceLines.add(line);
        }

        @Override
        public String toString() {
            return String.join(" ", experienceLines);
        }
    }

    private static class Contact {
        private static final Pattern emailPattern = Pattern.compile("\\S+@\\S+");
        private static final Pattern phonePattern = Pattern.compile("\\d{3}-\\d{3}-\\d{4}");

        public static List<String> getContactLines(List<String> lines) {
            return Stream.of(emailPattern, phonePattern).sequential()
                    .flatMap(pattern -> lines.stream().map(line -> find(line, pattern)))
                    .filter(Objects::nonNull)
                    .toList();
        }

        public static String findEmail(List<String> lines) {
            return lines.stream()
                    .map(line -> find(line, emailPattern))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        static String find(String str, Pattern pattern) {
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                return matcher.group();
            }
            return null;
        }

    }

}
