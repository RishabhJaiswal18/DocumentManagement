package com.document.DocumentManagementApplication.exception;

import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerInterceptor;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<String> handleMaxSizeException(MaxUploadSizeExceededException ex) {
		return new ResponseEntity<String>(
				"File size exceeds the maximum allowed size (e.g., 50MB). Please upload a smaller file.",
				HttpStatus.PAYLOAD_TOO_LARGE);
	}

	@ExceptionHandler(SizeLimitExceededException.class)
	public ResponseEntity<String> handleSizeLimitExceededException(SizeLimitExceededException ex) {
		return new ResponseEntity<String>("Request size exceeds limit! Try uploading fewer or smaller files.",
				HttpStatus.PAYLOAD_TOO_LARGE);
	}

	// Optional: Handle other general exceptions
	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleGenericException(Exception ex) {
		return new ResponseEntity<String>("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
