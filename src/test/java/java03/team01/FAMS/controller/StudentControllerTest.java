package java03.team01.FAMS.controller;

import java03.team01.FAMS.converter.StudentConverter;
import java03.team01.FAMS.model.entity.Student;
import java03.team01.FAMS.model.exception.FamsApiException;
import java03.team01.FAMS.model.payload.dto.StudentDto;
import java03.team01.FAMS.model.payload.dto.UpdateAttendingStatusDto;
import java03.team01.FAMS.service.StudentClassService;
import java03.team01.FAMS.service.StudentService;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class StudentControllerTest {
    @Mock
    private StudentService studentService;

    @Mock
    private  StudentClassService studentClassService;

    @Mock
    private StudentConverter studentConverter;

    @InjectMocks
    private StudentController studentController;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        studentController = new StudentController();
        Field field = StudentController.class.getDeclaredField("UPLOAD_DIR");
        field.setAccessible(true);
        field.set(studentController, "src/main/java/java03/team01/FAMS/data");
    }

    private Student createStudent() {
        return Student.builder()
                .id(1L)
                .studentCode("ABC123")
                .fullName("John Doe")
                .dob(LocalDate.of(2000, 1, 1))
                .gender("Male")
                .phone("1234567890")
                .email("john.doe@example.com")
                .school("Example School")
                .major("Computer Science")
                .graduatedDate(LocalDate.of(2022, 5, 15))
                .gpa(3.8F)
                .address("123 Main Street")
                .faAccount("example_account")
                .type("Type A")
                .status("Active")
                .reCer("Certificate XYZ")
                .joinedDate(LocalDate.of(2022, 6, 1))
                .area("Example Area")
                .build();
    }
    @Test
    public void testCreateStudent_Success() {
        // Create a dummy student DTO
        StudentDto studentDto = new StudentDto();

        // Create a dummy student DTO to be returned by the service
        StudentDto createdStudentDto = new StudentDto();
        createdStudentDto.setId(1L); // Set some dummy ID

        // Mock the behavior of the studentService.createStudent() method
        when(studentService.createStudent(any(StudentDto.class))).thenReturn(createdStudentDto);

        // Call the method under test
        ResponseEntity<?> responseEntity = studentController.createStudent(studentDto);

        // Verify the response
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(createdStudentDto, responseEntity.getBody());

        // Verify that the service method was called with the correct parameter
        verify(studentService, times(1)).createStudent(any(StudentDto.class));
    }

    @Test
    public void testCreateStudent_Exception() {
        // Create a dummy student DTO
        StudentDto studentDto = new StudentDto();

        // Mock the behavior of the studentService.createStudent() method to throw an exception
        when(studentService.createStudent(any(StudentDto.class))).thenThrow(new RuntimeException("Some error occurred"));

        // Call the method under test
        assertThrows(FamsApiException.class, () -> {
            studentController.createStudent(studentDto);
        });

        // Verify that the service method was called with the correct parameter
        verify(studentService, times(1)).createStudent(any(StudentDto.class));
    }

    @Test
    public void testDeleteStudent_Success() {
        String studentCode = "12345"; // Example student code
        when(studentService.deteleStudent(studentCode)).thenReturn(true);

        ResponseEntity<String> responseEntity = studentController.deleteStudent(studentCode);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Student deleted successfully.", responseEntity.getBody());
    }

    @Test
    public void testDeleteStudent_Failure() {
        String studentCode = "12345";
        when(studentService.deteleStudent(studentCode)).thenReturn(false);

        ResponseEntity<String> responseEntity = studentController.deleteStudent(studentCode);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Unable to delete student. Please check the student code or the student's status.", responseEntity.getBody());
    }

    @Test
    public void testViewListAllStudents_Success() {
        List<StudentDto> studentDTOList = createStudentDtoList();
        when(studentService.viewListAllStudent()).thenReturn(studentDTOList);

        ResponseEntity<List<StudentDto>> responseEntity = studentController.viewListAllStudents();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(studentDTOList, responseEntity.getBody());
    }

    @Test
    public void testViewListAllStudents_EmptyList() {
        List<StudentDto> studentDTOList = new ArrayList<>();
        when(studentService.viewListAllStudent()).thenReturn(studentDTOList);

        ResponseEntity<List<StudentDto>> responseEntity = studentController.viewListAllStudents();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(0, responseEntity.getBody().size());
    }

    @Test
    public void testViewListAllStudents_Exception() {
        when(studentService.viewListAllStudent()).thenThrow(new RuntimeException("Some error occurred"));

        ResponseEntity<List<StudentDto>> responseEntity = studentController.viewListAllStudents();

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(null, responseEntity.getBody());
    }

    private List<StudentDto> createStudentDtoList() {
        List<StudentDto> studentDtoList = new ArrayList<>();
        studentDtoList.add(createStudentDto());
        return studentDtoList;
    }

    private StudentDto createStudentDto() {
        return StudentDto.builder()
                .id(1L)
                .studentCode("ABC123")
                .fullName("John Doe")
                .dob(LocalDate.of(2000, 1, 1))
                .gender("Male")
                .phone("1234567890")
                .email("john.doe@example.com")
                .school("Example School")
                .major("Computer Science")
                .graduatedDate(LocalDate.of(2022, 5, 15))
                .gpa(3.8F)
                .address("123 Main Street")
                .faAccount("example_account")
                .type("Type A")
                .status("Active")
                .reCer("Certificate XYZ")
                .joinedDate(LocalDate.of(2022, 6, 1))
                .area("Example Area")
                .build();
    }

    @Test
    public void testUploadStudentsSuccess() throws IOException {
        // Tạo tệp CSV ảo
        String content = "id,studentCode,fullName,dob,gender,phone,email,school,major,graduatedDate,gpa,address,faAccount,type,status,reCer,joinedDate,area\n" +
                "1,ABC123,John Doe,2000-01-01,Male,1234567890,john.doe@example.com,Example School,Computer Science,2022-01-01,3.8,123 Main Street,example_account,Type A,Active,Certificate XYZ,2022-01-01,Example Area";
        Path tempFile = Files.createTempFile("students", ".csv");
        Files.write(tempFile, content.getBytes());

        // Tạo MultipartFile từ tệp CSV ảo
        MultipartFile multipartFile = new MockMultipartFile("file", "students.csv", "text/csv", Files.readAllBytes(tempFile));

        // Gọi phương thức uploadStudents()
        String result = studentController.uploadStudents(multipartFile);

        // Kiểm tra kết quả
        assertEquals("File uploaded successfully and students saved.", result);
    }

    @Test
    public void testUploadStudents_EmptyFile() throws Exception {
        MultipartFile multipartFile = new MockMultipartFile("file", "test.csv", "text/csv", new byte[0]);

        String result = studentController.uploadStudents(multipartFile);

        assertEquals("Please select a file to upload.", result);
    }

    @Test
    public void testEditInfoStudent_ValidStudent_ReturnsAccepted() {
        // Arrange
        StudentDto mockStudentDto = new StudentDto(/* add required fields */);
        long studentId = 1L;
        when(studentService.editInfoStudent(mockStudentDto, studentId)).thenReturn(mockStudentDto);

        // Act
        ResponseEntity<StudentDto> responseEntity = studentController.editInfoStudent(studentId, mockStudentDto);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());
        assertEquals(mockStudentDto, responseEntity.getBody());
    }



    @Test
    public void testViewStudentDetailsFound() {
        String studentCode = "ABC123";
        StudentDto student = createStudentDto();

        when(studentService.viewStudentDetails(studentCode)).thenReturn(student);

        ResponseEntity<?> responseEntity = studentController.viewStudentDetails(studentCode);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(student, responseEntity.getBody());
    }

    @Test
    public void testViewStudentDetailsNotFound() {
        String studentCode = "12345";

        when(studentService.viewStudentDetails(studentCode)).thenReturn(null);

        ResponseEntity<?> responseEntity = studentController.viewStudentDetails(studentCode);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void testViewStudentDetailsException() {
        // Arrange
        String studentCode = "S789";
        when(studentService.viewStudentDetails(studentCode)).thenThrow(new RuntimeException());

        // Act
        ResponseEntity<StudentDto> responseEntity = studentController.viewStudentDetails(studentCode);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    public void testViewListStudentByClassIdFound() {
        // Arrange
        Long classId = 1L;
        List<StudentDto> studentList = new ArrayList<>();
        studentList.add(new StudentDto());
        when(studentService.viewListStudentByClass(classId)).thenReturn(studentList);

        // Act
        ResponseEntity<List<StudentDto>> responseEntity = studentController.viewListStudentByClassId(classId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(studentList, responseEntity.getBody());
    }

    @Test
    public void testViewListStudentByClassIdNotFound() {
        // Arrange
        Long classId = 2L;
        when(studentService.viewListStudentByClass(classId)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<StudentDto>> responseEntity = studentController.viewListStudentByClassId(classId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void testViewListStudentByClassIdException() {
        // Arrange
        Long classId = 3L;
        when(studentService.viewListStudentByClass(classId)).thenThrow(new RuntimeException());

        // Act
        ResponseEntity<List<StudentDto>> responseEntity = studentController.viewListStudentByClassId(classId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }
    @Test
    public void testExportStudent_Success() {
        // Mock data
        List<Long> ids = new ArrayList<>();
        ids.add(1L);

        // Mock service behavior
        byte[] exportedData = new byte[10]; // Assume some dummy exported data
        when(studentService.exportStudent(ids)).thenReturn(exportedData);

        // Call the method
        ResponseEntity<byte[]> responseEntity = studentController.exportStudent(ids);

        // Verify the response
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
    }


    @Test
    public void testExportStudent_Exception() {
        // Call the method
        ResponseEntity<byte[]> responseEntity = studentController.exportStudent(null);

        // Verify the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
    }

    @Test
    public void testUpdateAttendingStatus_Success() {
        // Mock data
        UpdateAttendingStatusDto updateAttendingStatusDto = new UpdateAttendingStatusDto();
        updateAttendingStatusDto.setStudentIds(Arrays.asList(1L, 2L, 3L));
        updateAttendingStatusDto.setClassId(1L);
        updateAttendingStatusDto.setNewStatus("Present");
        when(studentClassService.updateAttendingStatusOfStudents(anyList(), anyLong(), anyString())).thenReturn(true);
        // Call the method
        ResponseEntity<?> responseEntity = studentController.updateAttendingStatusOfStudents(updateAttendingStatusDto);

        // Verify the response
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Update status successfully", responseEntity.getBody());
    }

    @Test
    public void testUpdateAttendingStatus_Failure() {
        // Mock data
        UpdateAttendingStatusDto updateAttendingStatusDto = new UpdateAttendingStatusDto();
        updateAttendingStatusDto.setStudentIds(Arrays.asList(1L, 2L, 3L));
        updateAttendingStatusDto.setClassId(1L);
        updateAttendingStatusDto.setNewStatus("Present");
        when(studentClassService.updateAttendingStatusOfStudents(anyList(), anyLong(), anyString())).thenReturn(false);
        // Call the method
        ResponseEntity<?> responseEntity = studentController.updateAttendingStatusOfStudents(updateAttendingStatusDto);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Cannot update status", responseEntity.getBody());
    }
    @Test
    public void testEditInfoStudent_InvalidStudent_ReturnsNotFound() {
        // Arrange
        StudentDto mockStudentDto = new StudentDto(/* add required fields */);
        long studentId = 1L;
        when(studentService.editInfoStudent(mockStudentDto, studentId)).thenReturn(null);

        // Act
        ResponseEntity<StudentDto> responseEntity = studentController.editInfoStudent(studentId, mockStudentDto);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(null, responseEntity.getBody()); // you can add more specific assertions here
    }
    @Test
    public void testViewListAllStudentsNotNull() {
        // Prepare sample data
        List<StudentDto> studentList = new ArrayList<>();
        // Add some sample students to the list

        // Mocking studentService behavior
        when(studentService.viewListAllStudent()).thenReturn(studentList);

        // Call the method to be tested
        ResponseEntity<List<StudentDto>> responseEntity = studentController.viewListAllStudents();

        // Verify that viewListAllStudent method of studentService is called
        verify(studentService, times(1)).viewListAllStudent();

        // Ensure that the response status is HttpStatus.OK
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Ensure that the list of students is returned properly
        assertEquals(studentList, responseEntity.getBody());
    }
}
