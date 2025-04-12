package com.document.DocumentManagementApplication.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.document.DocumentManagementApplication.entity.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

	@Query(value = "Select * from documents where text_content like concat('%',:keyword,'%')", nativeQuery = true)
	List<Document> searchByKeyword(@Param("keyword") String keyword);

	Page<Document> findByFileTypeAndFileNameContainingIgnoreCaseAndAuthorContainingIgnoreCaseAndInsertedDate(
			String fileType, String fileName, String author, String date, Pageable pageable);

	Page<Document> findByFileNameContainingIgnoreCase(String fileName, Pageable pageable);

	Page<Document> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

	Page<Document> findByInsertedDateContainingIgnoreCase(String date, Pageable pageable);
}
