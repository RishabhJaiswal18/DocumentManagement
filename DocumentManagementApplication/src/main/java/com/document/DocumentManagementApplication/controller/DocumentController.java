package com.document.DocumentManagementApplication.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.document.DocumentManagementApplication.entity.Document;
import com.document.DocumentManagementApplication.service.DocumentServiceInterface;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

	@Autowired
	DocumentServiceInterface documentService;

	@ApiOperation(value = "Upload Files (.text, .pdf, .doc)", notes = "This endpoint upload only (.pdf, .doc, .text)")
	@PostMapping("/upload")
	public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file,
			@RequestParam("author") String author) {
		try {
			if (file.isEmpty()) {
				return new ResponseEntity<String>("File is empty!", HttpStatus.BAD_REQUEST);
			}

			String status = documentService.uploadDocumentService(file, author);

			if (status.equalsIgnoreCase("success")) {
				return new ResponseEntity<String>("File uploaded successfully!", HttpStatus.OK);
			}

			return new ResponseEntity<String>("File uploading failed!", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			return new ResponseEntity<String>("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Search Documents By Keyword or Phrase")
	@GetMapping("/search")
	public ResponseEntity<List<Map<String, String>>> searchDocuments(@RequestParam String keyword) {
		List<Document> docs = documentService.searchByKeyword(keyword);

		List<Map<String, String>> response = docs.stream().map(doc -> {
			Map<String, String> map = new HashMap<>();
			map.put("fileName", doc.getFileName());
			map.put("fileType", doc.getFileType());

			String text = doc.getTextContent() != null ? doc.getTextContent() : "";
			String snippet = text.length() > 200 ? text.substring(0, 200) + "..." : text;
			map.put("snippet", snippet);

			return map;

		}).collect(Collectors.toList());

		if (!response.isEmpty()) {
			return new ResponseEntity<List<Map<String, String>>>(response, HttpStatus.OK);
		}

		return new ResponseEntity<List<Map<String, String>>>(response, HttpStatus.BAD_REQUEST);
	}

	@ApiOperation(value = "Filter Documents with file name or file type or author or date")
	@GetMapping("/documents")
	public ResponseEntity<List<Map<String, String>>> getDocuments(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "id,desc") String[] sort,
			@RequestParam(required = false) String fileType, @RequestParam(required = false) String fileName,
			@RequestParam(required = false) String author, @RequestParam(required = false) String date) {

		Sort.Direction direction = sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

		Page<Document> documentPage = documentService.getFilteredDocuments(fileType, fileName, author, date, pageable);

		List<Map<String, String>> result = new ArrayList<>();
		for (Document doc : documentPage.getContent()) {
			Map<String, String> map = new HashMap<>();
			map.put("fileName", doc.getFileName());
			map.put("fileType", doc.getFileType());

			String snippet = doc.getTextContent();
			if (snippet != null && snippet.length() > 200) {
				snippet = snippet.substring(0, 200) + "...";
			}
			map.put("snippet", snippet);

			result.add(map);
		}

		if (!result.isEmpty()) {
			return new ResponseEntity<List<Map<String, String>>>(result, HttpStatus.OK);
		}

		return new ResponseEntity<List<Map<String, String>>>(result, HttpStatus.BAD_REQUEST);
	}
}
