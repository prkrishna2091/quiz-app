package com.codapt.quizapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YoutubeCaptionDetails {
    private String caption;
    private String videoTitle;
    private String channelName;
    private String videoLength;
}

