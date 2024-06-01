package java03.team01.FAMS.exception;

import java03.team01.FAMS.model.exception.FamsApiException;
import java03.team01.FAMS.model.exception.GlobalExceptionHandler;
import java03.team01.FAMS.model.exception.ResourceNotFoundException;
import java03.team01.FAMS.model.payload.error.ErrorDetails;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.nio.file.AccessDeniedException;
import java.util.Date;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class GlobalExceptionTest {
    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

//    @Test
//    void handleResourceNotFoundException_ReturnsNotFoundResponse() {
//
//        when(studentRepository.findById(anyLong())).thenReturn(Optional.empty());
//
//        ResourceNotFoundException thrown = Assertions.assertThrows(ResourceNotFoundException.class, () -> reservationService.createReservation(reservedClassDto));
//
//        assertTrue(thrown.getMessage().contains("Student"));
//        verify(studentRepository, times(1)).findById(anyLong());
//
//        // Arrange
//        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found","resource",);
//        WebRequest request = mock(WebRequest.class);
//
//        // Act
//        ResponseEntity<ErrorDetails> response = globalExceptionHandler.handleResourceNotFoundException(ex, request);
//
//        // Assert
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//        assertEquals("Resource not found", response.getBody().getMessage());
//        // You can add more assertions based on your ErrorDetails implementation
//    }

    @Test
    void handleAccessDeniedException_ReturnsUnauthorizedResponse() {
        // Arrange
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        WebRequest request = mock(WebRequest.class);

        // Act
        ResponseEntity<ErrorDetails> response = globalExceptionHandler.handleAccessDeniedException(ex, request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Access denied", response.getBody().getMessage());

        // You can add more assertions based on your ErrorDetails implementation
    }

    @Test
    public void testHandleGlobalException() {
        // Given
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Exception ex = new Exception("Test exception message");
        WebRequest request = mock(WebRequest.class);

        // When
        ResponseEntity<ErrorDetails> responseEntity = handler.handleGlobalException(ex, request);

        // Then
        ErrorDetails errorDetails = responseEntity.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Test exception message", errorDetails.getMessage());
    }

    @Test
    public void testHandleFamsApiException() {
        // Given
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        FamsApiException ex = new FamsApiException(HttpStatus.BAD_REQUEST, "Test exception message");
        WebRequest request = mock(WebRequest.class);

        // When
        ResponseEntity<ErrorDetails> responseEntity = handler.handleFamsApiException(ex, request);

        // Then
        ErrorDetails errorDetails = responseEntity.getBody();
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Test exception message", errorDetails.getMessage());
        assertEquals(request.getDescription(false), errorDetails.getDetails());
    }

    @Test
    public void testHandleResourceNotFoundException() {
        // Given
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource", "id", 1);
        WebRequest request = mock(WebRequest.class);

        // When
        ResponseEntity<ErrorDetails> responseEntity = handler.handleResourceNotFoundException(ex, request);

        // Then
        ErrorDetails errorDetails = responseEntity.getBody();
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Resource not found with id: '1'", errorDetails.getMessage());
        assertEquals(request.getDescription(false), errorDetails.getDetails());
    }
}
