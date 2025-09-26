-- init-db.sql
-- This script runs when the DB container is first created.

-- Create the pgvector extension (if not already present)
CREATE EXTENSION IF NOT EXISTS vector;

-- Example table schema for chunks (you will create this via JPA or migrations too)
CREATE TABLE IF NOT EXISTS documents (
  id SERIAL PRIMARY KEY,
  filename TEXT,
  source TEXT,
  uploaded_at TIMESTAMP DEFAULT now()
);

CREATE TABLE IF NOT EXISTS chunks (
  id SERIAL PRIMARY KEY,
  document_id INTEGER REFERENCES documents(id),
  chunk_text TEXT,
  start_offset INTEGER,
  end_offset INTEGER,
  page INTEGER,
  embedding vector(1536), -- 1536 is example dimension (change if your embedding model uses different dim)
  created_at TIMESTAMP DEFAULT now()
);
