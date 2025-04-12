package com.document.DocumentManagementApplication.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.document.DocumentManagementApplication.entity.Document;
import com.document.DocumentManagementApplication.repository.DocumentRepository;

@Service
public class DocumentServiceImpl implements DocumentServiceInterface {

	@Autowired
	DocumentRepository documentRepository;

	@Async
	public CompletableFuture<String> extractText(MultipartFile file) throws IOException {
		String fileName = file.getOriginalFilename();
		assert fileName != null;

		if (fileName.endsWith(".txt")) {
			return CompletableFuture.completedFuture(new String(file.getBytes()));
		} else if (fileName.endsWith(".pdf")) {
			try (PDDocument document = PDDocument.load(file.getInputStream())) {
				return CompletableFuture.completedFuture(new PDFTextStripper().getText(document));
			}
		} else if (fileName.endsWith(".docx")) {
			try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
				StringBuilder text = new StringBuilder();
				doc.getParagraphs().forEach(p -> text.append(p.getText()).append("\n"));
				return CompletableFuture.completedFuture(text.toString());
			}
		} else {
			throw new IllegalArgumentException("Unsupported file format");
		}
	}

	@Override
	public String uploadDocumentService(MultipartFile file, String author) throws Exception {
		CompletableFuture<String> futureContent = extractText(file);
		String content = futureContent.get();

		Document document = new Document();
		document.setFileName(file.getOriginalFilename());
		document.setFileType(file.getContentType());
		document.setContent(content.getBytes());
		document.setTextContent(content);
		document.setAuthor(author);
		document.setInsertedDate(LocalDate.now().toString());

		Document saveAndFlush = documentRepository.saveAndFlush(document);

		if (saveAndFlush.getId() != 0) {
			return "success";
		}
		return "failed";
	}

	@Override
	public List<Document> searchByKeyword(String keyword) {
		return documentRepository.searchByKeyword(keyword);
	}

	@Override
	public Page<Document> getFilteredDocuments(String fileType, String fileName, String author, String date,
			Pageable pageable) {
		if (fileType != null && !fileType.isEmpty() && fileName != null && !fileName.isEmpty() && author != null
				&& !author.isEmpty() && date != null && !date.isEmpty()) {

			return documentRepository
					.findByFileTypeAndFileNameContainingIgnoreCaseAndAuthorContainingIgnoreCaseAndInsertedDate(fileType,
							fileName, author, date, pageable);
		} else if (fileName != null && !fileName.isEmpty()) {
			return documentRepository.findByFileNameContainingIgnoreCase(fileName, pageable);
		} else if (author != null && !author.isEmpty()) {
			return documentRepository.findByAuthorContainingIgnoreCase(author, pageable);
		} else if (date != null && !date.isEmpty()) {
			return documentRepository.findByInsertedDateContainingIgnoreCase(date, pageable);
		}

		return documentRepository.findAll(pageable);

	}

}
