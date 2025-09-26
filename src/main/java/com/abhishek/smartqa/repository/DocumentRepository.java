package com.abhishek.smartqa.repository;

import com.abhishek.smartqa.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {


}