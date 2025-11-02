package com.yh.sbps.api.config;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Centralized error handling returning meaningful JSON error responses. */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private Map<String, Object> baseBody(HttpStatus status, String message, String path) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", OffsetDateTime.now().toString());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    if (message != null && !message.isBlank()) {
      body.put("message", message);
    }
    if (path != null) {
      body.put("path", path);
    }
    return body;
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<Object> handleEntityNotFound(
      EntityNotFoundException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    Map<String, Object> body = baseBody(status, ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Object> handleResponseStatus(
      ResponseStatusException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
    if (status == null) {
      status = HttpStatus.BAD_REQUEST;
    }
    String message = ex.getReason();
    Map<String, Object> body = baseBody(status, message, request.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<Object> handleIllegalArgs(RuntimeException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    Map<String, Object> body = baseBody(status, ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Object> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    Map<String, Object> body = baseBody(status, "Validation failed", request.getRequestURI());
    body.put(
        "errors",
        ex.getConstraintViolations().stream()
            .map(
                v -> {
                  Map<String, Object> e = new HashMap<>();
                  e.put("field", v.getPropertyPath().toString());
                  e.put("message", v.getMessage());
                  return e;
                })
            .collect(Collectors.toList()));
    return ResponseEntity.status(status).body(body);
  }

  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    Map<String, Object> body =
        baseBody(httpStatus, "Validation failed", request.getDescription(false));
    body.put(
        "errors",
        ex.getBindingResult().getFieldErrors().stream()
            .map(this::fieldErrorToMap)
            .collect(Collectors.toList()));
    return ResponseEntity.status(httpStatus).body(body);
  }

  protected ResponseEntity<Object> handleBindException(
      BindException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    Map<String, Object> body =
        baseBody(httpStatus, "Validation failed", request.getDescription(false));
    body.put(
        "errors",
        ex.getBindingResult().getFieldErrors().stream()
            .map(this::fieldErrorToMap)
            .collect(Collectors.toList()));
    return ResponseEntity.status(httpStatus).body(body);
  }

  private Map<String, Object> fieldErrorToMap(FieldError fe) {
    Map<String, Object> e = new HashMap<>();
    e.put("field", fe.getField());
    e.put("message", fe.getDefaultMessage());
    return e;
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleAll(Exception ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    Map<String, Object> body = baseBody(status, ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }
}
