
package TraceNest.AI;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException exception) {

        Map<String, String> details = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        details.putIfAbsent(
                                error.getField(),
                                error.getDefaultMessage() == null
                                        ? "Invalid value"
                                        : error.getDefaultMessage()));

        Map<String, Object> response = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed");

        response.put("details", details);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException exception) {

        HttpStatus status = HttpStatus.resolve(
                exception.getStatusCode().value());

        String message = exception.getReason();

        if (message == null || message.isBlank()) {
            message = "The request could not be completed";
        }

        HttpStatus responseStatus =
                status == null
                        ? HttpStatus.INTERNAL_SERVER_ERROR
                        : status;

        Map<String, Object> response = createErrorResponse(
                responseStatus,
                message);

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequestBody(
            HttpMessageNotReadableException exception) {

        Map<String, Object> response = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request body. Check your JSON syntax and field values.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedException(
            Exception exception) {

        log.error("Unexpected error while handling a request", exception);

        Map<String, Object> response = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    private Map<String, Object> createErrorResponse(
            HttpStatus status,
            String message) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", OffsetDateTime.now().toString());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);

        return response;
    }
}