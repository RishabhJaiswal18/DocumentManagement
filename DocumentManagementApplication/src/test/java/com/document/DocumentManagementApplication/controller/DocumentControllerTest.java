package com.document.DocumentManagementApplication.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.document.DocumentManagementApplication.entity.Document;
import com.document.DocumentManagementApplication.service.DocumentServiceImpl;

@WebMvcTest(DocumentController.class)
public class DocumentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private DocumentServiceImpl documentService;

	@Test
	void testUploadTextFileSuccessfully() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Hello Test File".getBytes());
		String author = "John Doe";
		when(documentService.extractText(file)).thenReturn(CompletableFuture.completedFuture("Hello Test File"));
		when(documentService.uploadDocumentService(file, author)).thenReturn("success");

		mockMvc.perform(multipart("/api/documents/upload").file(file).param("author", author))
				.andExpect(status().isOk()).andExpect(content().string("File uploaded successfully!"));
	}

	@Test
	void testUploadFileWhenEmpty() throws Exception {
		MockMultipartFile emptyFile = new MockMultipartFile("file", "", "text/plain", new byte[0]);

		String author = "Test Author";

		mockMvc.perform(multipart("/api/documents/upload").file(emptyFile).param("author", author))
				.andExpect(status().isBadRequest()).andExpect(content().string("File is empty!"));
	}

	@Test
	void testUploadPdfFileSuccessfully() throws Exception {
		MockMultipartFile pdfFile = new MockMultipartFile("file", "test.pdf", "application/pdf",
				"PDF content".getBytes());
		String author = "Jane Doe";

		when(documentService.extractText(pdfFile)).thenReturn(CompletableFuture.completedFuture("PDF content"));
		when(documentService.uploadDocumentService(pdfFile, author)).thenReturn("success");

		mockMvc.perform(multipart("/api/documents/upload").file(pdfFile).param("author", author))
				.andExpect(status().isOk()).andExpect(content().string("File uploaded successfully!"));
	}

	@Test
	void testUploadDocxFileSuccessfully() throws Exception {
		MockMultipartFile docxFile = new MockMultipartFile("file", "test.docx",
				"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "DOCX content".getBytes());
		String author = "Alice";

		when(documentService.extractText(docxFile)).thenReturn(CompletableFuture.completedFuture("DOCX content"));
		when(documentService.uploadDocumentService(docxFile, author)).thenReturn("success");

		mockMvc.perform(multipart("/api/documents/upload").file(docxFile).param("author", author))
				.andExpect(status().isOk()).andExpect(content().string("File uploaded successfully!"));
	}

	@Test
	void testUploadUnsupportedFileType() throws Exception {
		MockMultipartFile exeFile = new MockMultipartFile("file", "test.exe", "application/octet-stream",
				"Binary content".getBytes());
		String author = "Hacker";

		// Let’s say your service still extracts text and stores it
		when(documentService.extractText(exeFile)).thenReturn(CompletableFuture.completedFuture("Binary content"));
		when(documentService.uploadDocumentService(exeFile, author)).thenReturn("failed");

		mockMvc.perform(multipart("/api/documents/upload").file(exeFile).param("author", author))
				.andExpect(status().isBadRequest()).andExpect(content().string("File uploading failed!"));
	}

	@Test
	void testUploadLargeFileSuccessfully() throws Exception {
		byte[] largeContent = new byte[30024 * 1024]; // 1MB fake content
		MockMultipartFile largeFile = new MockMultipartFile("file", "large.pdf", "application/pdf", largeContent);
		String author = "BigFileAuthor";

		when(documentService.extractText(largeFile)).thenReturn(CompletableFuture.completedFuture("Large file text"));
		when(documentService.uploadDocumentService(largeFile, author)).thenReturn("success");

		mockMvc.perform(multipart("/api/documents/upload").file(largeFile).param("author", author))
				.andExpect(status().isOk()).andExpect(content().string("File uploaded successfully!"));
	}

	@Test
	void testUploadServiceThrowsException() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Error file".getBytes());
		String author = "FailUser";

		when(documentService.extractText(file)).thenThrow(new RuntimeException("Simulated error"));

		mockMvc.perform(multipart("/api/documents/upload").file(file).param("author", author))
				.andExpect(status().isInternalServerError())
				.andExpect(content().string("An error occurred: Simulated error"));
	}

	@Test
	void testSearchDocuments() throws Exception {
		String keyword = "sample";

		Document document = new Document();
		document.setFileName("sample.txt");
		document.setAuthor("John");

		List<Document> mockResult = Collections.singletonList(document);

		when(documentService.searchByKeyword(keyword)).thenReturn(mockResult);

		mockMvc.perform(get("/api/documents/search").param("keyword", keyword)).andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$[0].fileName").value("sample.txt"))
				.andExpect(jsonPath("$[0].author").value("John"));
	}

	@Test
	void testGetDocumentsWithFilters() throws Exception {
		Document doc = new Document();
		doc.setId(1L);
		doc.setFileName("testFile.docx");
		doc.setAuthor("Jane Doe");
		doc.setFileType("docx");
		doc.setInsertedDate("2024-01-01");

		List<Document> documentList = new ArrayList<>();
		documentList.add(doc);
		Page<Document> documentPage = new PageImpl<>(documentList);

		when(documentService.getFilteredDocuments(eq("docx"), eq("testFile"), eq("Jane Doe"), eq("2024-01-01"),
				any(Pageable.class))).thenReturn(documentPage);

		mockMvc.perform(get("/api/documents").param("page", "0").param("size", "10").param("sort", "id", "desc")
				.param("fileType", "docx").param("fileName", "testFile").param("author", "Jane Doe")
				.param("date", "2024-01-01")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.content[0].fileName").value("testFile.docx"))
				.andExpect(jsonPath("$.content[0].author").value("Jane Doe"));
	}

}
