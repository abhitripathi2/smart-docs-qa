package com.abhishek.smartqa.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class ParserService {

    private final Tika tika = new Tika();

    /**
     * Extract plain text from uploaded file using Apache Tika.
     * This handles PDF, DOCX, PPTX, TXT, etc.
     */
    public String extractText(MultipartFile file) throws Exception {
        try (InputStream in = file.getInputStream()) {
            // Tika.detect can be used if needed for media type decisions
            String text = tika.parseToString(in);
            // basic normalization: remove excessive whitespace
            if (text == null) return "";
            return text.trim().replaceAll("\\s{2,}", " ");
        }
    }
}
