package com.codapt.quizapp.controller;

import com.codapt.quizapp.dto.YoutubeCaptionDetails;
import com.codapt.quizapp.dto.YoutubeQuizRequest;
import com.codapt.quizapp.dto.YoutubeQuizResponse;
import com.codapt.quizapp.service.GeminiService;
import com.codapt.quizapp.service.YoutubeCaptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quiz")
@Tag(name = "Quiz Controller", description = "Handles quiz generation from YouTube videos")
public class QuizController {

    private static final Logger logger = LoggerFactory.getLogger(QuizController.class);

    private final YoutubeCaptionService youtubeCaptionService;
    private final GeminiService geminiService;

    public QuizController(YoutubeCaptionService youtubeCaptionService, GeminiService geminiService) {
        this.youtubeCaptionService = youtubeCaptionService;
        this.geminiService = geminiService;
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate Quiz from YouTube URL",
            description = "Receives a YouTube URL, extracts captions, and generates a quiz using Gemini AI")
    public YoutubeQuizResponse generateQuiz(@RequestBody YoutubeQuizRequest request) {
        if (request == null || request.getYoutubeUrl() == null || request.getYoutubeUrl().trim().isEmpty()) {
            logger.warn("Invalid request: YouTube URL is required");
            throw new IllegalArgumentException("YouTube URL is required");
        }

        String url = request.getYoutubeUrl().trim();
        logger.info("Received quiz generation request for YouTube URL: {}", url);

        final YoutubeCaptionDetails captionDetails;
        try {
            captionDetails = youtubeCaptionService.downloadCaptions(url);
        } catch (Exception e) {
            logger.error("Failed to download captions and metadata for URL: {}", url, e);
            throw new RuntimeException("Failed to download captions from YouTube: " + e.getMessage(), e);
        }

        logger.info("Extracted YouTube metadata for URL {} - title: '{}', channel: '{}', length: {}",
                url,
                captionDetails.getVideoTitle(),
                captionDetails.getChannelName(),
                captionDetails.getVideoLength());

        String quiz = geminiService.getQuizFromGemini(captionDetails.getCaption());
        logger.debug("Successfully generated quiz for URL: {}", url);

        logger.info("Quiz generation completed successfully for URL: {}", url);
        return new YoutubeQuizResponse(
                captionDetails.getCaption(),
                quiz,
                captionDetails.getVideoTitle(),
                captionDetails.getChannelName(),
                captionDetails.getVideoLength());
    }
}
