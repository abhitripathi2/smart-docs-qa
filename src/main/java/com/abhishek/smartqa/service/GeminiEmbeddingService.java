package com.abhishek.smartqa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiEmbeddingService {

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent";

    private final WebClient webClient;

    public GeminiEmbeddingService(@Value("${gemini.api-key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl(GEMINI_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
    }

    /**
     * Batch embed many texts.
     * Retries a few times on transient failures.
     */
    public List<List<Double>> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) return List.of();

        // Build request payload matching the successful Postman format:
        Map<String,Object> body = Map.of(
                "model", "models/text-embedding-004",
                "content", Map.of("parts", texts.stream().map(t -> Map.of("text", t)).toList())
        );

        // simple retry loop
        int tries = 0;
        int maxTries = 3;
        long backoffMs = 500;
        while (true) {
            try {
                Map<?,?> resp = this.webClient.post()
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block(Duration.ofSeconds(30));

                if (resp == null || !resp.containsKey("embedding")) {
                    throw new RuntimeException("No embedding in response: " + resp);
                }

                // The response contains embedding.values (single embedding if one text)
                // For batch, some APIs return data/embeddings array. We handle common shapes.
                Object embeddingObj = resp.get("embedding");
                // If single embedding (when body had one part), wrap to list
                if (embeddingObj instanceof Map) {
                    List<List<Double>> out = new ArrayList<>();
                    List<?> values = (List<?>) ((Map<?,?>)embeddingObj).get("values");
                    List<Double> vec = new ArrayList<>();
                    for (Object v : values) vec.add(((Number) v).doubleValue());
                    out.add(vec);
                    return out;
                }

                // If response has data / embeddings array (some variants)
                if (resp.containsKey("data")) {
                    Object dataObj = resp.get("data");
                    if (dataObj instanceof List) {
                        List<List<Double>> out = new ArrayList<>();
                        for (Object item : (List<?>) dataObj) {
                            if (item instanceof Map) {
                                Object emb = ((Map<?,?>) item).get("embedding");
                                if (emb instanceof Map) {
                                    List<?> vals = (List<?>) ((Map<?,?>) emb).get("values");
                                    List<Double> vec = new ArrayList<>();
                                    for (Object v : vals) vec.add(((Number) v).doubleValue());
                                    out.add(vec);
                                }
                            }
                        }
                        if (!out.isEmpty()) return out;
                    }
                }

                // Fallback: check for "embeddings" array
                if (resp.containsKey("embeddings")) {
                    List<List<Double>> out = new ArrayList<>();
                    for (Object embItem : (List<?>) resp.get("embeddings")) {
                        if (embItem instanceof List) {
                            List<Double> vec = new ArrayList<>();
                            for (Object v : (List<?>) embItem) vec.add(((Number) v).doubleValue());
                            out.add(vec);
                        }
                    }
                    if (!out.isEmpty()) return out;
                }

                throw new RuntimeException("Unexpected embedding response shape: " + resp);
            } catch (Exception ex) {
                tries++;
                if (tries >= maxTries) {
                    throw new RuntimeException("Failed to get embeddings after " + tries + " tries: " + ex.getMessage(), ex);
                }
                try { Thread.sleep(backoffMs); } catch (InterruptedException ignored) {}
                backoffMs *= 2;
            }
        }
    }
}
