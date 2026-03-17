package com.codapt.quizapp.util;

public class PromptGenerator {

    public String promptTemplate= """
            You are an expert assessment designer and instructional evaluator.
            
            Your task is to generate a high-quality quiz based ONLY on the provided YouTube transcript content.
            
            You must follow all constraints strictly.
            
            ────────────────────────────
            TRANSCRIPT CONTENT:
            {{TRANSCRIPT_TEXT}}
            ────────────────────────────
            
            QUIZ REQUIREMENTS:
            - Generate {{QUESTION_COUNT}} multiple-choice questions.
            - Difficulty level: {{DIFFICULTY}} (easy / medium / hard).
            - Each question must test understanding, not surface memorization.
            - Avoid trivial facts unless clearly emphasized in the transcript.
            - Cover different key concepts from the content.
            - Do not invent information not present in the transcript.
            - Each question must have exactly 4 answer choices.
            - Only one answer is correct.
            - Provide a concise explanation (1–3 sentences) for the correct answer.
            
            QUESTION DESIGN RULES:
            - Mix conceptual, applied, and reasoning-based questions.
            - If the transcript includes examples, convert them into scenario-based questions.
            - If the content is technical, prioritize mechanism and cause-effect reasoning.
            - Do NOT repeat similar questions.
            - Do NOT reference timestamps or mention “the transcript”.
            
            OUTPUT FORMAT:
            Return ONLY valid JSON.
            Do NOT include markdown.
            Do NOT include commentary.
            Do NOT include explanations outside the JSON.
            
            Use this exact schema:
            
            {
              "title": "Quiz Title Based on Topic",
              "questions": [
                {
                  "id": "q1",
                  "question": "Question text here?",
                  "options": [
                    "Option A",
                    "Option B",
                    "Option C",
                    "Option D"
                  ],
                  "correctAnswerIndex": 0,
                  "explanation": "Brief explanation of why the answer is correct."
                }
              ]
            }
            
            Ensure:
            - JSON is syntactically valid.
            - correctAnswerIndex is 0–3.
            - Exactly {{QUESTION_COUNT}} questions are returned.
            """;
}
