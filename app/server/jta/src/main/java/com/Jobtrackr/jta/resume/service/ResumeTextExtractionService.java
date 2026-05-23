package com.Jobtrackr.jta.resume.service;

import com.Jobtrackr.jta.exception.BadRequestException;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class ResumeTextExtractionService {

    private static final Logger log = LoggerFactory.getLogger(ResumeTextExtractionService.class);
    private final Tika tika = new Tika();

    public String extractText(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new BadRequestException("Resume file path is missing");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            throw new BadRequestException("Resume file not found on disk");
        }

        try {
            String text = tika.parseToString(file);
            if (text == null || text.isBlank()) {
                throw new BadRequestException("Unable to extract text from the uploaded resume");
            }
            return text;
        } catch (IOException | TikaException e) {
            log.error("Failed to extract text from resume file: {}", filePath, e);
            throw new BadRequestException("Failed to read resume file");
        }
    }
}
