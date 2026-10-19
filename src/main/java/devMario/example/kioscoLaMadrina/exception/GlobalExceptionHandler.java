package devMario.example.kioscoLaMadrina.exception;

import devMario.example.kioscoLaMadrina.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import devMario.example.kioscoLaMadrina.security.ratelimit.RetryAfter;
import devMario.example.kioscoLaMadrina.security.ratelimit.TooManyLoginAttemptsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Convierte cualquier excepción en una respuesta JSON con el formato de la API ({@link ErrorResponseDTO}).
 *
 * Hereda de ResponseEntityExceptionHandler (patrón Template Method): Spring ya sabe resolver los errores
 * del protocolo HTTP (método no permitido, tipo de contenido, parámetros mal tipados, validación...) con su
 * código y encabezados correctos, y llama al gancho {@link #handleExceptionInternal} para armar la respuesta.
 * Sobrescribiendo solo ese gancho, todos esos casos usan nuestro formato y mensajes en español.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflictException(ConflictException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // Mensaje único a propósito: Spring valida "cuenta desactivada" ANTES que la contraseña,
    // así que un mensaje específico revelaría el estado de una cuenta sin conocer su clave.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", request);
    }

    // Login bloqueado por intentos fallidos: 429 + Retry-After (estándar HTTP) para que el cliente sepa cuándo reintentar.
    @ExceptionHandler(TooManyLoginAttemptsException.class)
    public ResponseEntity<ErrorResponseDTO> handleTooManyLoginAttempts(TooManyLoginAttemptsException ex,
                                                                      HttpServletRequest request) {
        long seconds = RetryAfter.seconds(ex.getRetryAfter());
        long minutes = Math.ceilDiv(seconds, 60);
        String message = "Demasiados intentos fallidos. Volvé a intentar en " + minutes
                + (minutes == 1 ? " minuto." : " minutos.");

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(seconds))
                .body(errorBody(HttpStatus.TOO_MANY_REQUESTS, message, request.getRequestURI()));
    }

    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "No tienes permisos suficientes para realizar esta acción.", request);
    }

    // Cualquier otra excepción es un error real del servidor: se registra completa para poder corregirla,
    // pero el cliente recibe un mensaje genérico (sin detalles internos).
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado en el servidor. Por favor, contacta al soporte técnico.", request);
    }

    /**
     * Gancho del Template Method: Spring ya decidió el código HTTP y los encabezados (p. ej. Allow en un 405);
     * acá solo se arma el cuerpo con el formato de la API.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        return new ResponseEntity<>(errorBody(status, protocolErrorMessage(ex), path), headers, statusCode);
    }

    private static String protocolErrorMessage(Exception ex) {
        return switch (ex) {
            case MethodArgumentNotValidException e -> e.getBindingResult().getFieldErrors().stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            case MethodArgumentTypeMismatchException e -> "Valor inválido para '%s': %s".formatted(e.getName(), e.getValue());
            case TypeMismatchException e -> "Valor inválido para '%s': %s".formatted(e.getPropertyName(), e.getValue());
            // JSON mal formado o valores inválidos para un enum (p. ej. un rol inexistente).
            case HttpMessageNotReadableException e -> "El cuerpo de la petición es inválido";
            case HttpRequestMethodNotSupportedException e -> "Método HTTP no permitido para esta ruta";
            case HttpMediaTypeNotSupportedException e -> "Tipo de contenido no soportado: enviá application/json";
            case NoResourceFoundException e -> "El recurso solicitado no existe";
            default -> "La petición no es válida";
        };
    }

    private ResponseEntity<ErrorResponseDTO> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
        return new ResponseEntity<>(errorBody(status, message, request.getRequestURI()), status);
    }

    /** Único lugar que define el formato de los errores de la API. */
    private static ErrorResponseDTO errorBody(HttpStatus status, String message, String path) {
        return new ErrorResponseDTO(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path);
    }
}
