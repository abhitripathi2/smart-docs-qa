package com.abhishek.smartqa.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmbeddingStorageService {

    private final JdbcTemplate jdbcTemplate;

    public EmbeddingStorageService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private String toVectorLiteral(List<Double> vector) {
        return "[" + vector.stream().map(Object::toString).collect(Collectors.joining(",")) + "]";
    }

    public void saveNewChunk(Long documentId, String chunkText, int startOffset, int endOffset, Integer page, List<Double> embedding) {
        String sql = "INSERT INTO chunks (document_id, chunk_text, start_offset, end_offset, page, embedding) VALUES (?, ?, ?, ?, ?, CAST(? AS vector))";
        jdbcTemplate.update(sql, documentId, chunkText, startOffset, endOffset, page, toVectorLiteral(embedding));
    }

    public void saveChunkEmbedding(Long chunkId, List<Double> embedding) {
        String sql = "UPDATE chunks SET embedding = CAST(? AS vector) WHERE id = ?";
        jdbcTemplate.update(sql, toVectorLiteral(embedding), chunkId);
    }
}
