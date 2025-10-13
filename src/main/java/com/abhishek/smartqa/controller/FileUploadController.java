package com.abhishek.smartqa.controller;

import com.abhishek.smartqa.model.Document;
import com.abhishek.smartqa.service.ChunkingService;
import com.abhishek.smartqa.service.EmbeddingStorageService;
import com.abhishek.smartqa.service.GeminiEmbeddingService;
import com.abhishek.smartqa.service.ParserService;
import com.abhishek.smartqa.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    private final ParserService parserService;
    private final ChunkingService chunkingService;
    private final StorageService storageService; // existing service that creates Document metadata
    private final GeminiEmbeddingService geminiEmbeddingService;
    private final EmbeddingStorageService embeddingStorageService;

    public FileUploadController(ParserService parserService,
                                ChunkingService chunkingService,
                                StorageService storageService,
                                GeminiEmbeddingService geminiEmbeddingService,
                                EmbeddingStorageService embeddingStorageService) {
        this.parserService = parserService;
        this.chunkingService = chunkingService;
        this.storageService = storageService;
        this.geminiEmbeddingService = geminiEmbeddingService;
        this.embeddingStorageService = embeddingStorageService;
    }

    /**
     * Upload -> parse -> chunk -> batch embed -> insert chunks with embeddings
     */
    @PostMapping("/upload")
    @Transactional // optional: wrap document+chunks insertion in one transaction
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            Document doc = storageService.createDocument(filename, "upload"); // ensure this creates and returns the Document with id

            String fullText = parserService.extractText(file);
            List<ChunkingService.TextChunk> chunks = chunkingService.chunkText(fullText);

            // prepare texts for batching
            List<String> texts = chunks.stream().map(ChunkingService.TextChunk::getText).collect(Collectors.toList());

            // Batch embeddings (single call)
            List<List<Double>> embeddings = geminiEmbeddingService.embedBatch(texts);

            if (embeddings.size() != texts.size()) {
                // Defensive check — log and fail if mismatch
                throw new RuntimeException("Embedding count mismatch: texts=" + texts.size() + " embeddings=" + embeddings.size());
            }

            for (int i = 0; i < chunks.size(); i++) {
                ChunkingService.TextChunk c = chunks.get(i);
                List<Double> emb = embeddings.get(i);
                embeddingStorageService.saveNewChunk(doc.getId(), c.getText(), c.getStartOffset(), c.getEndOffset(), c.getPage(), emb);
            }

            return ResponseEntity.ok(Map.of("docId", doc.getId(), "chunks", chunks.size()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
}
