package com.abhishek.smartqa.controller;

import com.abhishek.smartqa.dto.ChunkCreateRequest;
import com.abhishek.smartqa.dto.DocumentCreateRequest;
import com.abhishek.smartqa.model.Chunk;
import com.abhishek.smartqa.model.Document;
import com.abhishek.smartqa.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StorageService storageService;

    public ApiController(StorageService storageService) {
        this.storageService = storageService;
    }

    // Create a new document (metadata)
    @PostMapping("/documents")
    public ResponseEntity<Document> createDocument(@RequestBody DocumentCreateRequest req) {
        Document doc = storageService.createDocument(req.getFilename(), req.getSource());
        return ResponseEntity.ok(doc);
    }

    // Add a chunk to an existing document
    @PostMapping("/chunks")
    public ResponseEntity<Chunk> createChunk(@RequestBody ChunkCreateRequest req) {
        Chunk chunk = storageService.createChunk(
                req.getDocumentId(),
                req.getChunkText(),
                req.getStartOffset(),
                req.getEndOffset(),
                req.getPage(),
                req.getEmbedding()
        );
        return ResponseEntity.ok(chunk);
    }

    // List all documents
    @GetMapping("/documents")
    public ResponseEntity<List<Document>> listDocuments() {
        return ResponseEntity.ok(storageService.listDocuments());
    }

    // Get a document with its chunks
    @GetMapping("/documents/{id}")
    public ResponseEntity<Document> getDocument(@PathVariable Long id) {
        Document doc = storageService.getDocument(id);
        if (doc == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(doc);
    }

    // List all chunks
    @GetMapping("/chunks")
    public ResponseEntity<List<Chunk>> listChunks() {
        return ResponseEntity.ok(storageService.listChunks());
    }
}
