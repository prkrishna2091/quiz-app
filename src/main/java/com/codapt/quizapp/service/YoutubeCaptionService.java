package com.codapt.quizapp.service;

import com.codapt.quizapp.dto.YoutubeCaptionDetails;

public interface YoutubeCaptionService {
    YoutubeCaptionDetails downloadCaptions(String youtubeUrl) throws Exception;
}
