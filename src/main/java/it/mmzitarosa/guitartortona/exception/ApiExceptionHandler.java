package it.mmzitarosa.guitartortona.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException e) {
		return body(HttpStatus.NOT_FOUND, e.getCode(), e.getMessage());
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<Map<String, Object>> handleConflict(ConflictException e) {
		return body(HttpStatus.CONFLICT, e.getCode(), e.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.map(f -> f.getField() + ": " + f.getDefaultMessage())
				.reduce((a, b) -> a + "; " + b).orElse("Validation error");
		return body(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
	}

	private ResponseEntity<Map<String, Object>> body(HttpStatus status, String code, String message) {
		return ResponseEntity.status(status).body(Map.of("status", status.value(), "code", code, "message", message));
	}
}
