package com.abhishek.smartqa.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SemanticSearchService {

    private final JdbcTemplate jdbcTemplate;

    public SemanticSearchService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private String toVectorLiteral(List<Double> vector) {
        return "[" + vector.stream()
                .map(Object::toString)
                .collect(Collectors.joining(",")) + "]";
    }

    public List<Map<String, Object>> searchSimilarChunks(List<Double> queryVector, int limit) {
        String sql = """
            SELECT chunk_text, embedding <-> CAST(? AS vector) AS distance
            FROM chunks
            ORDER BY embedding <-> CAST(? AS vector)
            LIMIT ?
        """;

        String vectorLiteral = toVectorLiteral(queryVector);
        return jdbcTemplate.queryForList(sql, vectorLiteral, vectorLiteral, limit);
    }
}
