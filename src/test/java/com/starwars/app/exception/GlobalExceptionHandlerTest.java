package com.starwars.app.exception;

import com.starwars.app.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    void shouldHandleValidationErrors() throws Exception {
        Object target = new Object();
        String objectName = "user";
        BindingResult bindingResult = new BeanPropertyBindingResult(target, objectName);
        bindingResult.addError(new FieldError(objectName, "username", "Username is required"));
        bindingResult.addError(new FieldError(objectName, "email", "Invalid email format"));

        MethodParameter methodParameter = new MethodParameter(
                this.getClass().getDeclaredMethod("dummyMethod"), -1);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationErrors(
                exception, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.getStatus()).isEqualTo(400);
        assertThat(errorResponse.getError()).isEqualTo("Validation Failed");
        assertThat(errorResponse.getMessage()).isEqualTo("Invalid input data");
        assertThat(errorResponse.getPath()).isEqualTo("/api/test");
        assertThat(errorResponse.getTimestamp()).isNotNull();
        assertThat(errorResponse.getDetails()).hasSize(2);
        assertThat(errorResponse.getDetails()).contains(
                "username: Username is required",
                "email: Invalid email format"
        );
    }

    public void dummyMethod() {}

    @Test
    void shouldHandleBadCredentialsException() {
        BadCredentialsException exception = new BadCredentialsException("Invalid credentials");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBadCredentials(
                exception, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();

        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.getStatus()).isEqualTo(401);
        assertThat(errorResponse.getError()).isEqualTo("Authentication Failed");
        assertThat(errorResponse.getMessage()).isEqualTo("Invalid username/email or password");
        assertThat(errorResponse.getPath()).isEqualTo("/api/test");
        assertThat(errorResponse.getTimestamp()).isNotNull();
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Username already exists: luke");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgument(
                exception, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();

        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.getStatus()).isEqualTo(409);
        assertThat(errorResponse.getError()).isEqualTo("Conflict");
        assertThat(errorResponse.getMessage()).isEqualTo("Username already exists: luke");
        assertThat(errorResponse.getPath()).isEqualTo("/api/test");
        assertThat(errorResponse.getTimestamp()).isNotNull();
    }

    @Test
    void shouldHandleAccessDeniedException() {
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccessDenied(
                exception, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();

        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.getStatus()).isEqualTo(403);
        assertThat(errorResponse.getError()).isEqualTo("Access Denied");
        assertThat(errorResponse.getMessage()).isEqualTo("Authentication token is required to access this resource");
        assertThat(errorResponse.getPath()).isEqualTo("/api/test");
        assertThat(errorResponse.getTimestamp()).isNotNull();
    }

    @Test
    void shouldHandleGenericException() {
        RuntimeException exception = new RuntimeException("Something went wrong");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(
                exception, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();

        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.getStatus()).isEqualTo(500);
        assertThat(errorResponse.getError()).isEqualTo("Internal Server Error");
        assertThat(errorResponse.getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(errorResponse.getPath()).isEqualTo("/api/test");
        assertThat(errorResponse.getTimestamp()).isNotNull();
    }

    @Test
    void shouldHandleNullPointerException() {
        NullPointerException exception = new NullPointerException("Null pointer");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(
                exception, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();

        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.getStatus()).isEqualTo(500);
        assertThat(errorResponse.getError()).isEqualTo("Internal Server Error");
        assertThat(errorResponse.getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(errorResponse.getPath()).isEqualTo("/api/test");
    }

    @Test
    void shouldHandleValidationErrorsWithEmptyFieldErrors() throws Exception {
        Object target = new Object();
        String objectName = "user";
        BindingResult bindingResult = new BeanPropertyBindingResult(target, objectName);

        MethodParameter methodParameter = new MethodParameter(
                this.getClass().getDeclaredMethod("dummyMethod"), -1);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationErrors(
                exception, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetails()).isEmpty();
    }

    @Test
    void shouldHandleValidationErrorsWithSingleFieldError() throws Exception {
        Object target = new Object();
        String objectName = "user";
        BindingResult bindingResult = new BeanPropertyBindingResult(target, objectName);
        bindingResult.addError(new FieldError(objectName, "password", "Password must be at least 6 characters"));

        MethodParameter methodParameter = new MethodParameter(
                this.getClass().getDeclaredMethod("dummyMethod"), -1);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationErrors(
                exception, webRequest);

        assertThat(response.getBody().getDetails()).hasSize(1);
        assertThat(response.getBody().getDetails().get(0)).isEqualTo("password: Password must be at least 6 characters");
    }

    @Test
    void shouldStripUriPrefixFromPath() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/auth/login");
        BadCredentialsException exception = new BadCredentialsException("Invalid credentials");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBadCredentials(
                exception, webRequest);

        assertThat(response.getBody().getPath()).isEqualTo("/api/auth/login");
    }

    @Test
    void shouldHandlePathWithoutUriPrefix() {
        when(webRequest.getDescription(false)).thenReturn("/api/users");
        IllegalArgumentException exception = new IllegalArgumentException("User not found");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgument(
                exception, webRequest);

        assertThat(response.getBody().getPath()).isEqualTo("/api/users");
    }

    @Test
    void shouldSetTimestampForValidationErrors() throws Exception {
        Object target = new Object();
        String objectName = "user";
        BindingResult bindingResult = new BeanPropertyBindingResult(target, objectName);
        bindingResult.addError(new FieldError(objectName, "email", "Invalid email"));

        MethodParameter methodParameter = new MethodParameter(
                this.getClass().getDeclaredMethod("dummyMethod"), -1);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        LocalDateTime beforeCall = LocalDateTime.now().minusSeconds(1);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationErrors(
                exception, webRequest);

        LocalDateTime afterCall = LocalDateTime.now().plusSeconds(1);
        assertThat(response.getBody().getTimestamp()).isBetween(beforeCall, afterCall);
    }

    @Test
    void shouldHandleMultipleValidationErrorsForSameField() throws Exception {
        Object target = new Object();
        String objectName = "user";
        BindingResult bindingResult = new BeanPropertyBindingResult(target, objectName);
        bindingResult.addError(new FieldError(objectName, "username", "Username is required"));
        bindingResult.addError(new FieldError(objectName, "username", "Username must be at least 3 characters"));
        bindingResult.addError(new FieldError(objectName, "email", "Email is required"));

        MethodParameter methodParameter = new MethodParameter(
                this.getClass().getDeclaredMethod("dummyMethod"), -1);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationErrors(
                exception, webRequest);

        assertThat(response.getBody().getDetails()).hasSize(3);
        assertThat(response.getBody().getDetails()).contains(
                "username: Username is required",
                "username: Username must be at least 3 characters",
                "email: Email is required"
        );
    }

    @Test
    void shouldHandleExceptionWithNullMessage() {
        IllegalArgumentException exception = new IllegalArgumentException();

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgument(
                exception, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).isNull();
    }
}