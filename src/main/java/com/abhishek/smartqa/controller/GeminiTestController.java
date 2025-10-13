package com.abhishek.smartqa.controller;

import com.abhishek.smartqa.service.GeminiEmbeddingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class GeminiTestController {

    private final GeminiEmbeddingService geminiService;

    public GeminiTestController(GeminiEmbeddingService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/embedding")
    public List<Double> testEmbedding(@RequestParam String text) {
        return geminiService.generateEmbedding(text);
    }
}
