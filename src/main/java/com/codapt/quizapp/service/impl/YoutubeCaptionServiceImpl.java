package com.codapt.quizapp.service.impl;

import com.codapt.quizapp.dto.YoutubeCaptionDetails;
import com.codapt.quizapp.service.YoutubeCaptionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;


@Service
public class YoutubeCaptionServiceImpl implements YoutubeCaptionService {

    private static final Logger logger = LoggerFactory.getLogger(YoutubeCaptionServiceImpl.class);

    @Override
    public YoutubeCaptionDetails downloadCaptions(String youtubeUrl) throws Exception {
        logger.info("Starting caption download for YouTube URL: {}", youtubeUrl);

        // Load yt-dlp from resources
        ClassPathResource resource = new ClassPathResource("yt-dlp.exe");
        File exeFile = File.createTempFile("yt-dlp", ".exe");

        try (InputStream in = resource.getInputStream();
             OutputStream out = new FileOutputStream(exeFile)) {
            in.transferTo(out);
        }

        exeFile.setExecutable(true);
        logger.debug("yt-dlp executable prepared at: {}", exeFile.getAbsolutePath());

        // Run yt-dlp --dump-json
        ProcessBuilder pb = new ProcessBuilder(
                exeFile.getAbsolutePath(),
                "--dump-json",
                youtubeUrl
        );

        Process process = pb.start();
        logger.debug("yt-dlp process started for URL: {}", youtubeUrl);

        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
        }

        int exitCode = process.waitFor();
        logger.debug("yt-dlp process completed with exit code: {}", exitCode);

        if (exitCode != 0) {
            logger.error("yt-dlp failed with exit code: {}", exitCode);
            throw new RuntimeException("Failed to retrieve video information from yt-dlp");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json.toString());

        String videoTitle = root.path("title").asText("Unknown title");
        String channelName = root.path("channel").asText("Unknown channel");
        long durationSeconds = root.path("duration").asLong(0);
        String videoLength = formatDuration(durationSeconds);

        logger.info("Fetched YouTube metadata - title: '{}', channel: '{}', length: {}",
                videoTitle, channelName, videoLength);

        JsonNode subtitles = root.path("subtitles").path("en");
        String subtitleUrl = null;
        if (subtitles.isArray() && subtitles.size() > 0) {
            subtitleUrl = subtitles.get(0).path("url").asText();
        }

        if (subtitleUrl == null || subtitleUrl.isEmpty()) {
            JsonNode autoCaptions = root.path("automatic_captions").path("en");
            if (autoCaptions.isArray() && autoCaptions.size() > 0) {
                subtitleUrl = autoCaptions.get(0).path("url").asText();
            }
        }

        if (subtitleUrl == null || subtitleUrl.isEmpty()) {
            logger.warn("No captions found for video: {}", youtubeUrl);
            return new YoutubeCaptionDetails(
                    "No captions available for this video",
                    videoTitle,
                    channelName,
                    videoLength
            );
        }

        logger.info("Captions found, downloading from: {}", subtitleUrl);

        StringBuilder captions = new StringBuilder();
        try (BufferedReader subtitleReader = new BufferedReader(new InputStreamReader(new URL(subtitleUrl).openStream()))) {
            String line;
            while ((line = subtitleReader.readLine()) != null) {
                captions.append(line).append("\n");
            }
        }

        logger.info("Captions downloaded successfully for video: {}", youtubeUrl);

        String cleanedCaptions = captions.toString()
                .replaceAll("WEBVTT", "")
                .replaceAll("\\d{2}:\\d{2}:\\d{2}\\.\\d+ --> .*", "")
                .replaceAll("<.*?>", "")
                .trim();

        return new YoutubeCaptionDetails(cleanedCaptions, videoTitle, channelName, videoLength);
    }

    private String formatDuration(long durationSeconds) {
        if (durationSeconds <= 0) {
            return "00:00";
        }

        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        long seconds = durationSeconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }

        return String.format("%02d:%02d", minutes, seconds);
    }
}
