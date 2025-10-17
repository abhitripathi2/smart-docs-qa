package com.abhishek.smartqa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.stream.Collectors;

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

        // Pre-filter: keep index mapping so we can return embeddings in original order
        List<Integer> originalIndexes = new ArrayList<>();
        List<String> nonEmptyTexts = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            String t = texts.get(i);
            if (t != null && !t.trim().isEmpty()) {
                nonEmptyTexts.add(t);
                originalIndexes.add(i);
            }
        }

        // If no non-empty texts, return empty lists aligned to original inputs
        if (nonEmptyTexts.isEmpty()) {
            return texts.stream().map(t -> List.<Double>of()).toList();
        }

        final int batchSize = 8; // tuneable: 8-16 is a good start
        List<List<Double>> resultsForNonEmpty = new ArrayList<>(nonEmptyTexts.size());

        for (int start = 0; start < nonEmptyTexts.size(); start += batchSize) {
            int end = Math.min(start + batchSize, nonEmptyTexts.size());
            List<String> batch = nonEmptyTexts.subList(start, end);

            // Attempt batch call; if mismatch or unexpected shape, fallback to per-item
            List<List<Double>> batchEmbeddings = null;
            try {
                batchEmbeddings = callGeminiForBatch(batch);
            } catch (Exception e) {
                System.err.println("Batch embedding failed, will fallback to single calls. Err: " + e.getMessage());
                batchEmbeddings = null;
            }

            if (batchEmbeddings == null || batchEmbeddings.size() != batch.size()) {
                // fallback: call each item individually
                List<List<Double>> fallback = new ArrayList<>();
                for (String single : batch) {
                    try {
                        List<List<Double>> singleResp = callGeminiForBatch(List.of(single));
                        if (singleResp != null && !singleResp.isEmpty()) fallback.add(singleResp.get(0));
                        else fallback.add(List.of());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        fallback.add(List.of());
                    }
                }
                resultsForNonEmpty.addAll(fallback);
            } else {
                resultsForNonEmpty.addAll(batchEmbeddings);
            }
        }

        // Build final result aligned to original texts: empty inputs get empty list
        List<List<Double>> finalResults = new ArrayList<>(texts.size());
        // initialize with empty lists
        for (int i = 0; i < texts.size(); i++) finalResults.add(List.of());

        for (int i = 0; i < nonEmptyTexts.size(); i++) {
            int origIndex = originalIndexes.get(i);
            finalResults.set(origIndex, resultsForNonEmpty.get(i));
        }

        return finalResults;
    }

    /**
     * Helper that actually performs the Gemini call for a batch of texts and parses response.
     * Returns a list of embeddings in the same order as 'batch' or throws on error.
     */
    @SuppressWarnings("unchecked")
    private List<List<Double>> callGeminiForBatch(List<String> batch) {
        // Build request payload with content.parts
        Map<String,Object> body = Map.of(
                "model", "models/text-embedding-004",
                "content", Map.of("parts", batch.stream().map(t -> Map.of("text", t)).toList())
        );

        // Logging request sizes for debugging
        System.out.println("Gemini batch request size=" + batch.size() + ", sample lengths=" +
                batch.stream().map(String::length).map(Object::toString).collect(Collectors.joining(",")));

        Map<?,?> resp = this.webClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(30));

        System.out.println("Gemini raw response: " + resp);

        if (resp == null) throw new RuntimeException("No response from Gemini");

        // Attempt to parse common shapes.
        // 1) If response contains "embedding" -> single embedding (for whole content)
        if (resp.containsKey("embedding")) {
            Object embeddingObj = resp.get("embedding");
            if (embeddingObj instanceof Map) {
                List<?> values = (List<?>) ((Map<?,?>) embeddingObj).get("values");
                List<Double> vec = toDoubleList(values);
                // If batch had more than 1 text but API returned 1 embedding, return single embedding
                // caller will detect size mismatch and fallback if necessary.
                return List.of(vec);
            }
        }

        // 2) If response has "data" array with items containing embeddings
        if (resp.containsKey("data") && resp.get("data") instanceof List) {
            List<List<Double>> out = new ArrayList<>();
            for (Object item : (List<?>) resp.get("data")) {
                if (item instanceof Map) {
                    Object emb = ((Map<?,?>) item).get("embedding");
                    if (emb instanceof Map) {
                        List<?> vals = (List<?>) ((Map<?,?>) emb).get("values");
                        out.add(toDoubleList(vals));
                    } else if (emb instanceof List) {
                        out.add(toDoubleList((List<?>) emb));
                    }
                }
            }
            if (!out.isEmpty()) return out;
        }

        // 3) If response has "embeddings" as array of arrays
        if (resp.containsKey("embeddings") && resp.get("embeddings") instanceof List) {
            List<List<Double>> out = new ArrayList<>();
            for (Object embItem : (List<?>) resp.get("embeddings")) {
                if (embItem instanceof List) {
                    out.add(toDoubleList((List<?>) embItem));
                }
            }
            if (!out.isEmpty()) return out;
        }

        // 4) fallback -> unexpected shape
        throw new RuntimeException("Unexpected embedding response shape: " + resp);
    }

    private List<Double> toDoubleList(List<?> vals) {
        List<Double> vec = new ArrayList<>();
        if (vals == null) return vec;
        for (Object v : vals) {
            if (v == null) continue;
            if (v instanceof Number) vec.add(((Number) v).doubleValue());
            else try { vec.add(Double.parseDouble(v.toString())); } catch (Exception ignored) {}
        }
        return vec;
    }

}