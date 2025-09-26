package com.abhishek.smartqa.model;

import jakarta.persistence.*;

@Entity
@Table(name = "chunks")
public class Chunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(columnDefinition = "text")
    private String chunkText;

    private Integer startOffset;
    private Integer endOffset;
    private Integer page;

    // Store embedding later (pgvector column).
    // For now, keep as JSON string until we add pgvector support.
    @Column(columnDefinition = "text")
    private String embedding;

    // Getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public String getChunkText() { return chunkText; }
    public void setChunkText(String chunkText) { this.chunkText = chunkText; }

    public Integer getStartOffset() { return startOffset; }
    public void setStartOffset(Integer startOffset) { this.startOffset = startOffset; }

    public Integer getEndOffset() { return endOffset; }
    public void setEndOffset(Integer endOffset) { this.endOffset = endOffset; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public String getEmbedding() { return embedding; }
    public void setEmbedding(String embedding) { this.embedding = embedding; }
}
