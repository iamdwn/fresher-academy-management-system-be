package java03.team01.FAMS.controller;

import java03.team01.FAMS.model.entity.Score;
import java03.team01.FAMS.model.payload.requestModel.CustomCertificateRequest;
import java03.team01.FAMS.model.payload.requestModel.CustomScoreRequest;
import java03.team01.FAMS.model.payload.responseModel.CustomObjectResponse;
import java03.team01.FAMS.model.payload.responseModel.ImportExcelResponse;
import java03.team01.FAMS.model.payload.responseModel.ReservedStudentResponse;
import java03.team01.FAMS.model.payload.responseModel.ScoreClassResponse;
import java03.team01.FAMS.repository.ScoreRepository;
import java03.team01.FAMS.service.ScoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class ScoreControllerTest {

    @Mock
    private ScoreService scoreService;

    @Mock
    private ScoreRepository scoreRepository;

    @InjectMocks
    private ScoreController scoreController;

    @Test
    public void testGetScoreOfClass(){
        // Mock input parameters
        int pageNo = 0;
        int pageSize = 10;
        Long classId = 1L;

        // Mock service response
        ScoreClassResponse mockResponse = new ScoreClassResponse();
        when(scoreService.getScoreOfClass(anyInt(), anyInt(), anyLong())).thenReturn(mockResponse);

        // Call controller method
        ResponseEntity<ScoreClassResponse> responseEntity = scoreController.getScoreOfClass(pageNo, pageSize, classId);

        // Verify service method is called with correct arguments
        verify(scoreService).getScoreOfClass(pageNo, pageSize, classId);

        // Verify response entity
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertSame(mockResponse, responseEntity.getBody());

    }

    @Test
    public void testUpdateScoreOfClass() {
        // Mock request object
        Long scoreId = 3L;
        Float scoreValue = 2F;


        // Mock service response
        CustomObjectResponse mockResponse = new CustomObjectResponse();
        when(scoreService.updateScoreOfClass(any(CustomScoreRequest.class))).thenReturn(mockResponse);

        // Call controller method
        ResponseEntity<CustomObjectResponse> responseEntity = scoreController.updateScoreOfClass(scoreId, scoreValue);

        // Verify service method is called with correct argument
//        CustomScoreRequest request = new CustomScoreRequest(scoreId, scoreValue);
//        verify(scoreService).updateScoreOfClass(request);

        // Verify response entity
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertSame(mockResponse, responseEntity.getBody());
    }

    @Test
    public void testUpdateCertificate(){
        //String token = "mock-token";
        CustomCertificateRequest request = new CustomCertificateRequest();

        CustomObjectResponse mockResponse = new CustomObjectResponse();
        when(scoreService.updateCertificate(any(CustomCertificateRequest.class))).thenReturn(mockResponse);

        ResponseEntity<CustomObjectResponse> responseEntity = scoreController.updateCertificate(request);

        verify(scoreService).updateCertificate(request);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertSame(mockResponse, responseEntity.getBody());
    }

    @Test
    public  void testGetScoreByClassIdAndStudentCode(){
        Long classId = 1L;
        String studentCode = "SE123";
        CustomObjectResponse mockResponse = new CustomObjectResponse();
        when(scoreService.getScoreOfClassAndStudentCode(anyLong(), anyString())).thenReturn(mockResponse);

        ResponseEntity<CustomObjectResponse> responseEntity = scoreController.getScoreOfClass(classId, studentCode);

        verify(scoreService).getScoreOfClassAndStudentCode(classId, studentCode);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertSame(mockResponse, responseEntity.getBody());
    }

    // Test import file success
    @Test
    public void testImportScores_Success() throws IOException {
        byte[] fileContent = "Test file content".getBytes();
        MultipartFile multipartFile = new MockMultipartFile("testFile.xlsx", fileContent);

        ImportExcelResponse importResponse = new ImportExcelResponse();
        List<String> successList = new ArrayList<>();
        successList.add("Record 1");
        importResponse.setSuccessList(successList);

        when(scoreService.uploadFile(any(MultipartFile.class))).thenReturn(importResponse);

        ResponseEntity<ImportExcelResponse> responseEntity = scoreController.importScores(multipartFile);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(importResponse, responseEntity.getBody());
    }

    // Test download file failed
    @Test
    public void testDownloadExcel_Exception(){
        when(scoreRepository.findAll()).thenThrow(new RuntimeException("Mock Exception"));

        ResponseEntity<InputStreamResource> responseEntity = scoreController.downloadExcel();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
    }

    //Test download file success
    @Test
    public void testDownloadExcel_Success() throws IOException {
        List<Score> scoreList = new ArrayList<>();

        ByteArrayInputStream excelStream = new ByteArrayInputStream("mock_excel_data".getBytes());
        when(scoreService.generateExcelFromScores(scoreList)).thenReturn(excelStream);

        ResponseEntity<InputStreamResource> responseEntity = scoreController.downloadExcel();

        HttpHeaders headers = responseEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, headers.getContentType());
        String contentDisposition = headers.getContentDisposition().toString();
        assertTrue(contentDisposition.contains("attachment"));
        assertTrue(contentDisposition.contains("filename=\"scores.xlsx\""));

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    public void testDownloadTemplateExcel_Fail() throws IOException {
        // Mock ScoreService
        ScoreService scoreService = mock(ScoreService.class);
        ByteArrayInputStream excelStream = new ByteArrayInputStream("Test".getBytes());
        when(scoreService.generateTemplateExcel()).thenReturn(excelStream);

        // Instantiate YourClass
        ScoreController scoreController = new ScoreController();

        // Invoke the method
        ResponseEntity<InputStreamResource> response = scoreController.downloadTemplateExcel();

        // Assertions
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testDownloadTemplateExcel_Success() throws IOException {
        ByteArrayInputStream excelStream = new ByteArrayInputStream("mock_excel_data".getBytes());
        when(scoreService.generateTemplateExcel()).thenReturn(excelStream);

        ResponseEntity<InputStreamResource> responseEntity = scoreController.downloadTemplateExcel();

        HttpHeaders headers = responseEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, headers.getContentType());
        String contentDisposition = headers.getContentDisposition().toString();
        assertTrue(contentDisposition.contains("attachment"));
        assertTrue(contentDisposition.contains("filename=\"scores.xlsx\""));

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
    }
}
