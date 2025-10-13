package com.abhishek.smartqa.dto;

public class ChunkCreateRequest {
    private Long documentId;
    private String chunkText;
    private Integer startOffset;
    private Integer endOffset;
    private Integer page;
    private String embedding; // JSON string for now, e.g., "[0.1,0.2,...]"

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

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
