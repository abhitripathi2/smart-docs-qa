package com.abhishek.smartqa.controller;

import com.abhishek.smartqa.service.GeminiEmbeddingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class GeminiTestController {

    private final GeminiEmbeddingService geminiService;

    public GeminiTestController(GeminiEmbeddingService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/api/test/embedding")
    public Map<String, Object> testEmbedding(@RequestParam String text) {
        List<List<Double>> embeddings = geminiService.embedBatch(List.of(text));
        List<Double> embedding = embeddings.get(0);
        return Map.of(
                "text", text,
                "embedding_size", embedding.size(),
                "sample_values", embedding.subList(0, Math.min(5, embedding.size()))
        );
    }
}
