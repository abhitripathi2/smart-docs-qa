package com.abhishek.smartqa.dto;

public class DocumentCreateRequest {
    private String filename;
    private String source;

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
