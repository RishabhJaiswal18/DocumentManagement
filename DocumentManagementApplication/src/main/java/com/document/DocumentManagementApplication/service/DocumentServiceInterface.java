package com.document.DocumentManagementApplication.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.document.DocumentManagementApplication.entity.Document;

public interface DocumentServiceInterface {

	public String uploadDocumentService(MultipartFile file, String author) throws Exception;

	public List<Document> searchByKeyword(String keyword);

	public Page<Document> getFilteredDocuments(String fileType, String fileName, String author, String date,
			Pageable pageable);
}
