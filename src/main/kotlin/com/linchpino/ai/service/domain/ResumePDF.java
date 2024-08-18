package com.linchpino.ai.service.domain;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResumePDF {

    private final Logger logger = LoggerFactory.getLogger(ResumePDF.class);

    private final String filePath;
    private static final String[] HEADERS = {"About", "Summary", "Experience", "Education", "Top Skills", "Skills", "Certifications", "Projects", "Publications", "Contact", "Recommendations", "Licenses & certifications"};
    private Map<String, String> parsedProfile;

    public ResumePDF(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            logger.error("File path cannot be null or empty!");
            throw new IllegalArgumentException("File path cannot be null or empty!");
        }
        this.filePath = filePath;
    }

    public String getFullText() {
        try (PDDocument document = Loader.loadPDF(new File(getClass().getClassLoader().getResource(filePath).getFile()))) {
            PDFTextStripper textStripper = new PDFTextStripper();
            return textStripper.getText(document);
        } catch (IOException e) {
            String message = "Error occurred while reading the file!";
            logger.error(message, e);
            return message;
        }
    }

    public String getSummary() {
        if (parsedProfile == null) {
            parsedProfile = parse(getFullText());
        }
        return parsedProfile.get("Summary");
    }

    public String getExperience() {
        if (parsedProfile == null) {
            parsedProfile = parse(getFullText());
        }
        return parsedProfile.get("Experience");
    }

    private Map<String, String> parse(String fullText) {
        String[] lines = fullText.split("\n");
        Map<String, List<String>> headersAndContents = new HashMap<>();
        headersAndContents.put("No title", new ArrayList<>());
        int lineCount = lines.length;
        for (int lineCntr = 0; lineCntr < lineCount; lineCntr++) {
            String header = lineIsHeader(lines[lineCntr]);
            if (header == null) {
                headersAndContents.get("No title").add(lines[lineCntr]);
                continue;
            }
            headersAndContents.put(header, new ArrayList<>());
            for (int restLineCntr = lineCntr + 1; restLineCntr < lineCount; restLineCntr++) {
                if (lineIsHeader(lines[restLineCntr]) != null) {
                    break;
                }
                headersAndContents.get(header).add(lines[restLineCntr]);
                lineCntr++;
            }
        }
        Map<String, String> headersAndContentsAsString = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : headersAndContents.entrySet()) {
            headersAndContentsAsString.put(entry.getKey(), String.join(" ", entry.getValue()));
        }
        return headersAndContentsAsString;
    }

    private String lineIsHeader(String line) {
        for (String header : HEADERS) {
            if (line.toLowerCase().contains(header.toLowerCase()) &&
                    line.length() < 2 * header.length()) {
                return header;
            }
        }
        return null;
    }
}
