package com.abhishek.smartqa.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple sliding-window chunker.
 * - chunkSize: number of characters per chunk (tune later for tokens)
 * - overlap: number of characters overlap
 *
 * For production use, replace with token-based chunking using the same tokenizer as your embedding model.
 */
@Service
public class ChunkingService {

    // tune these — 2000 characters ~ ~300-500 tokens depending on content
    private final int chunkSize = 1500;
    private final int overlap = 200;

    public static class TextChunk {
        private final String text;
        private final int startOffset;
        private final int endOffset;
        private final Integer page; // unknown for now; keep null if not detected

        public TextChunk(String text, int startOffset, int endOffset, Integer page) {
            this.text = text;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.page = page;
        }

        public String getText() { return text; }
        public int getStartOffset() { return startOffset; }
        public int getEndOffset() { return endOffset; }
        public Integer getPage() { return page; }
    }

    public List<TextChunk> chunkText(String input) {
        List<TextChunk> chunks = new ArrayList<>();
        if (input == null || input.isBlank()) return chunks;

        String text = input.trim();
        int length = text.length();
        int start = 0;
        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            String chunk = text.substring(start, end).trim();
            chunks.add(new TextChunk(chunk, start, end, null));
            // advance start by chunkSize - overlap
            if (end == length) break;
            start = Math.max(0, end - overlap);
        }
        return chunks;
    }
}
