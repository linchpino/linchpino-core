package com.linchpino.ai.model;

import com.linchpino.core.exception.ErrorCode;
import com.linchpino.core.exception.LinchpinException;
import jakarta.persistence.*;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
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
        if (email == null || lines == null) {
            throw new LinchpinException(ErrorCode.SERVER_ERROR, "Email and lines must not be null");
        }
        this.email = email;
        this.lines = lines.stream().map(Resume::removeInvalidUtf8Bytes).toList();
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

    public static String removeInvalidUtf8Bytes(String input) {
        if (input == null) {
            return null;
        }
        try {
            // Create a CharsetDecoder to handle invalid byte sequences
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
            decoder.onMalformedInput(CodingErrorAction.IGNORE);
            decoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
            byte[] bytes = input.replace("\u0000", "").getBytes(StandardCharsets.UTF_8);
            return decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            throw new LinchpinException(ErrorCode.SERVER_ERROR, e.getMessage());
        }
    }
}
