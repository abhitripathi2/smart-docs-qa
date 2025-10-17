package com.abhishek.smartqa.controller;

import com.abhishek.smartqa.service.GeminiEmbeddingService;
import com.abhishek.smartqa.service.SemanticSearchService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class QaController {

    private final GeminiEmbeddingService geminiEmbeddingService;
    private final SemanticSearchService semanticSearchService;

    public QaController(GeminiEmbeddingService geminiEmbeddingService,
                        SemanticSearchService semanticSearchService) {
        this.geminiEmbeddingService = geminiEmbeddingService;
        this.semanticSearchService = semanticSearchService;
    }

    @PostMapping("/qa")
    public Map<String, Object> askQuestion(@RequestBody Map<String, String> body) {
        String question = body.get("question");

        // 1️⃣ Get embedding of the user question
        var questionEmbedding = geminiEmbeddingService.embedBatch(java.util.List.of(question)).get(0);

        // 2️⃣ Search similar chunks in PostgreSQL
        var topChunks = semanticSearchService.searchSimilarChunks(questionEmbedding, 3);

        return Map.of(
                "question", question,
                "results", topChunks
        );
    }
}
