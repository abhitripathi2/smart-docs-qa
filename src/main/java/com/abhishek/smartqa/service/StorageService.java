package com.abhishek.smartqa.service;

import com.abhishek.smartqa.model.Chunk;
import com.abhishek.smartqa.model.Document;
import com.abhishek.smartqa.repository.ChunkRepository;
import com.abhishek.smartqa.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StorageService {

    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;

    public StorageService(DocumentRepository documentRepository, ChunkRepository chunkRepository) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
    }

    public Document createDocument(String filename, String source) {
        Document d = new Document();
        d.setFilename(filename);
        d.setSource(source);
        return documentRepository.save(d);
    }

    public Chunk createChunk(Long documentId, String chunkText, Integer startOffset,
                             Integer endOffset, Integer page, String embeddingJson) {
        Optional<Document> docOpt = documentRepository.findById(documentId);
        if (docOpt.isEmpty()) {
            throw new IllegalArgumentException("Document with id " + documentId + " not found");
        }
        Chunk c = new Chunk();
        c.setDocument(docOpt.get());
        c.setChunkText(chunkText);
        c.setStartOffset(startOffset);
        c.setEndOffset(endOffset);
        c.setPage(page);
        c.setEmbedding(embeddingJson); // store as JSON text for now
        return chunkRepository.save(c);
    }

    public List<Document> listDocuments() {
        return documentRepository.findAll();
    }

    public Document getDocument(Long id) {
        return documentRepository.findById(id).orElse(null);
    }

    public List<Chunk> listChunks() {
        return chunkRepository.findAll();
    }
}
