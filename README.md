# DocumentManagement
A Spring Boot application to upload, store, search, and filter documents with support for .txt, .pdf, and .docx files.  
Includes Swagger documentation, JUnit test cases, and async text extraction.

---

## ✅ Features

- Upload documents with author and type metadata
- Extract text content from `.txt`, `.pdf`, `.docx`
- Store documents in MySQL database
- Search documents using keyword (on extracted content)
- Filter documents by file type, name, author, or upload date
- Pagination and sorting
- Swagger UI documentation
- Asynchronous processing using `@Async`
- Global exception handling

---

## ⚙️ Tech Stack

- Java 8
- Spring Boot 2.7
- Spring Data JPA
- MySQL
- Swagger (Springfox 2.9.2)
- JUnit 5 / Mockito
- Maven

---

## 🚀 API Flow Overview

### 1. **Upload Document**
- **Endpoint**: `POST /api/documents/upload`
- Accepts a file (`MultipartFile`) and author name
- Extracts text content from file
- Saves file metadata + text in DB
- Asynchronous processing enabled via `@Async`

### 2. **Search Documents**
- **Endpoint**: `GET /api/documents/search`
- Accepts `keyword` in query param
- Searches keyword in `textContent` column
- Returns matched documents with snippets

### 3. **Filter Documents**
- **Endpoint**: `GET /api/documents`
- Supports filtering by `fileType`, `fileName`, `author`, `date`
- Supports pagination and sorting
- Combines filters using dynamic JPA query

---

## 🧩 Architecture Flow

```plaintext
User (Swagger/Postman/Frontend)
        |
        v
   DocumentController
        |
        v
   DocumentServiceImpl (Business logic)
        |
        v
   DocumentRepository (Spring Data JPA)
        |
        v
   MySQL Database