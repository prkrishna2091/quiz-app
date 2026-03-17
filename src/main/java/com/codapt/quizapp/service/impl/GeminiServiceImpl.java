package com.codapt.quizapp.service.impl;

import com.codapt.quizapp.service.GeminiService;
import com.codapt.quizapp.util.PromptGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class GeminiServiceImpl implements GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiServiceImpl.class);

    private final ChatClient chatClient;
    private final PromptGenerator promptGenerator = new PromptGenerator();

    public GeminiServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        logger.info("GeminiServiceImpl initialized with ChatClient");
    }

    @Override
    public String getQuizFromGemini(String prompt) {
        logger.debug("Generating quiz prompt from transcript");

        String geminiPrompt = promptGenerator.promptTemplate;
        geminiPrompt = geminiPrompt.replace("{{TRANSCRIPT_TEXT}}", prompt);
        geminiPrompt = geminiPrompt.replace("{{QUESTION_COUNT}}", "10");
        geminiPrompt = geminiPrompt.replace("{{DIFFICULTY}}", "HARD");

        logger.debug("Calling Gemini AI with generated prompt");

        try {
            String response = chatClient.prompt()
                    .user(geminiPrompt)
                    .call()
                    .content();

            logger.info("Successfully generated quiz response from Gemini AI");
            logger.debug("Response length: {} characters", response.length());
            return response;
        } catch (Exception e) {
            String message = e.getMessage() == null ? "Unknown Gemini error" : e.getMessage();
            if (isUnsupportedModelError(e)) {
                logger.error("Gemini model is not available for this API key/project. Update spring.ai.google.genai.chat.options.model. Error: {}", message);
                throw new RuntimeException("Configured Gemini model is not supported for this API key/project. Please update spring.ai.google.genai.chat.options.model.", e);
            }

            logger.error("Error calling Gemini AI: {}", message, e);
            throw new RuntimeException("Failed to generate quiz from Gemini AI: " + message, e);
        }
    }

    private boolean isUnsupportedModelError(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String msg = current.getMessage();
            if (msg != null) {
                String lowerMessage = msg.toLowerCase(Locale.ROOT);
                if (lowerMessage.contains("models/") && lowerMessage.contains("not found")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}
