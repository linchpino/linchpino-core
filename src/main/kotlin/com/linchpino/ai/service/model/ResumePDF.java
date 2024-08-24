package com.linchpino.ai.service.model;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResumePDF {

    private final Logger logger = LoggerFactory.getLogger(ResumePDF.class);

    private static final String[] ALL_HEADERS = {"About", "Summary", "Experience", "Education", "Top Skills", "Skills", "Certifications", "Projects", "Publications", "Contact", "Recommendations", "Licenses & certifications"};

    private final List<String> resumeLines;

    public ResumePDF(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        resumeLines = readLines(file);
    }

    private List<String> readLines(File file) {
        try (PDDocument document = Loader.loadPDF(file)) {
            logger.info("Reading the file: {}", file.getName());
            PDFTextStripper textStripper = new PDFTextStripper();
            return new ArrayList<>(Arrays.asList(textStripper.getText(document).split("\n")));
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while reading the file!", e);
        }
    }

    public String getFullText() {
        return String.join(" ", resumeLines);
    }

    public String getSummary() {
        return parse(resumeLines, new Summary()).toString();
    }

    public String getExperience() {
        return parse(resumeLines, new Experience()).toString();
    }

    private Section parse(List<String> textLines, Section section) {
        for (int lineCntr = 0; lineCntr < textLines.size(); lineCntr++) {
            String header = lineIsHeader(textLines.get(lineCntr), section.getHeaders());
            if (header == null) {
                continue;
            }
            for (int restLineCntr = lineCntr + 1; restLineCntr < textLines.size(); restLineCntr++) {
                String restLine = textLines.get(restLineCntr);
                if (lineIsHeader(restLine, ALL_HEADERS) != null) {
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

    private class Summary implements Section {

        private final String[] headers = {"About", "Summary", "About me", "Professional Summary", "Objective", "Career Summary", "Profile"};
        private List<String> summaryLines = new ArrayList<>();

        public List<String> getSummaryLines() {
            return summaryLines;
        }

        public void setSummaryLines(List<String> summaryLines) {
            this.summaryLines = summaryLines;
        }

        @Override
        public String[] getHeaders() {
            return headers;
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

    private class Experience implements Section {

        private final String[] headers = {"Experience", "Work Experience", "Professional Experience", "Employment", "Work History"};
        private List<String> experienceLines = new ArrayList<>();

        public List<String> getExperienceLines() {
            return experienceLines;
        }

        public void setExperienceLines(List<String> experienceLines) {
            this.experienceLines = experienceLines;
        }

        @Override
        public String[] getHeaders() {
            return headers;
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

}
