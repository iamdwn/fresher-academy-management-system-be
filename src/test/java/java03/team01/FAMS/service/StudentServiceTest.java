package java03.team01.FAMS.service;


import java03.team01.FAMS.converter.StudentConverter;
import java03.team01.FAMS.model.entity.Student;
import java03.team01.FAMS.model.entity.StudentClass;
import java03.team01.FAMS.model.payload.dto.StudentDto;
import java03.team01.FAMS.repository.ScoreRepository;
import java03.team01.FAMS.repository.StudentClassRepository;
import java03.team01.FAMS.repository.StudentRepository;
import java03.team01.FAMS.service.impl.StudentClassServiceImpl;
import java03.team01.FAMS.service.impl.StudentServiceImpl;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StudentServiceTest {

    @Mock
    StudentConverter studentConverter;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentClassRepository studentClassRepository;
    @InjectMocks
    private StudentClassServiceImpl studentClassService;

    @Mock
    private ScoreRepository scoreRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    @BeforeEach
    public void init(){
        MockitoAnnotations.openMocks(this);
    }



    private StudentDto createMockStudent(String fullName, String email){
        StudentDto dto = new StudentDto();
        dto.setFullName(fullName);
        dto.setEmail(email);
        return dto;
    }
    @Test
    public void testCreateStudent() {
        // Create a dummy student DTO
        StudentDto studentDto = new StudentDto();
        studentDto.setId(1L); // Set some dummy ID

        // Create a dummy student entity to be returned by the converter
        Student studentEntity = new Student();
        studentEntity.setId(studentDto.getId());

        // Mock the behavior of the studentConverter
        when(studentConverter.toEntity(any(StudentDto.class))).thenReturn(studentEntity);
        when(studentConverter.toDto(any(Student.class))).thenReturn(studentDto);

        // Call the method under test
        StudentDto createdStudentDto = studentService.createStudent(studentDto);

        // Verify the behavior
        assertEquals(studentDto, createdStudentDto);

        // Verify that the converter methods were called with the correct parameters
        verify(studentConverter, times(1)).toEntity(any(StudentDto.class));
        verify(studentConverter, times(1)).toDto(any(Student.class));

        // Verify that the repository method save() was called with the correct parameter
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    public void testViewListAllStudent() {
        List<Student> sampleStudentList = createStudentList();

        when(studentRepository.findAll()).thenReturn(sampleStudentList);

        List<StudentDto> expectedStudentDTOList = createStudentDtoList();

        when(studentConverter.convertToStudentDTOList(sampleStudentList)).thenReturn(expectedStudentDTOList);

        List<StudentDto> actualStudentDTOList = studentService.viewListAllStudent();

        verify(studentRepository, times(1)).findAll();

        verify(studentConverter, times(1)).convertToStudentDTOList(sampleStudentList);

        assertEquals(expectedStudentDTOList, actualStudentDTOList);
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

    private List<Student> createStudentList() {
        List<Student> studentList = new ArrayList<>();
        studentList.add(createStudent());
        return studentList;
    }

    private List<StudentDto> createStudentDtoList() {
        List<StudentDto> studentDtoList = new ArrayList<>();
        studentDtoList.add(createStudentDto());
        return studentDtoList;
    }

    @Test
    public void testDeleteNonExistentStudent() {
        // Arrange
        String nonExistentStudentCode = "XYZ789";
        when(studentRepository.getStudentByStudentCode(nonExistentStudentCode)).thenReturn(null);

        // Act
        boolean result = studentService.deteleStudent(nonExistentStudentCode);

        // Assert
        verify(studentRepository, never()).delete(any(Student.class));
        assertFalse(result);
    }

    @Test
    public void testDeleteActiveStudent() {
        // Arrange
        String activeStudentCode = "ABC123";
        Student activeStudent = createStudent();
        activeStudent.setStatus("Active");
        when(studentRepository.getStudentByStudentCode(activeStudentCode)).thenReturn(activeStudent);

        // Mocking the studentRepository.delete() method
        doNothing().when(studentRepository).delete(activeStudent);

        // Act
        boolean result = studentService.deteleStudent(activeStudentCode);

        // Assert
        verify(studentRepository, times(1)).delete(activeStudent);
        assertTrue(result);
    }


    @Test
    public void testDeleteInactiveStudentWithNoScore() {
        // Arrange
        String inactiveStudentCode = "GHI789";
        Student inactiveStudent = createStudent();
        inactiveStudent.setStatus("Inactive");
        when(studentRepository.getStudentByStudentCode(inactiveStudentCode)).thenReturn(inactiveStudent);
        when(scoreRepository.getScoreByStudentId(inactiveStudent.getId())).thenReturn(null);

        // Act
        boolean result = studentService.deteleStudent(inactiveStudentCode);

        // Assert
        verify(studentRepository).delete(inactiveStudent);
        assertTrue(result);
    }
    @Test
    public void testDeleteStudentWithScoreExists() {
        String studentCode = "ABC123";
        Student sampleStudent = createStudent();
        sampleStudent.setStatus("active");



        boolean result = studentService.deteleStudent(studentCode);

        verify(studentRepository, never()).delete(sampleStudent);

        assertFalse(result);
    }


    @Test
    public void testReadStudentsFromCSV() throws IOException {
        String csvData = "123 Main Street,Example Area,2000/01/01,john.doe@example.com,example_account,John Doe,Male,3.8,2022/05/15,2022/06/01,Computer Science,1234567890,Certificate XYZ,Example School,Active,ABC123,Type A\n";

        File tempFile = File.createTempFile("temp", ".csv");
        tempFile.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write(csvData);
        writer.close();

        List<StudentDto> studentDTOList = studentService.readStudentsFromCSV(tempFile.getAbsolutePath());

        assertFalse(studentDTOList.isEmpty());

        StudentDto firstStudentDTO = studentDTOList.get(0);
        assertEquals("123 Main Street", firstStudentDTO.getAddress());
        assertEquals("Example Area", firstStudentDTO.getArea());
        assertEquals(LocalDate.of(2000, 1, 1), firstStudentDTO.getDob());
        assertEquals("john.doe@example.com", firstStudentDTO.getEmail());
        assertEquals("example_account", firstStudentDTO.getFaAccount());
        assertEquals("John Doe", firstStudentDTO.getFullName());
        assertEquals("Male", firstStudentDTO.getGender());
        Assert.assertEquals(3.8f, firstStudentDTO.getGpa(), 0.001f);
        assertEquals(LocalDate.of(2022, 5, 15), firstStudentDTO.getGraduatedDate());
        assertEquals(LocalDate.of(2022, 6, 1), firstStudentDTO.getJoinedDate());
        assertEquals("Computer Science", firstStudentDTO.getMajor());
        assertEquals("1234567890", firstStudentDTO.getPhone());
        assertEquals("Certificate XYZ", firstStudentDTO.getReCer());
        assertEquals("Example School", firstStudentDTO.getSchool());
        assertEquals("Active", firstStudentDTO.getStatus());
        assertEquals("ABC123", firstStudentDTO.getStudentCode());
        assertEquals("Type A", firstStudentDTO.getType());
    }

    @Test
    public void testViewStudentDetails() {
        // Given
        String studentCode = "ABC123";
        Student student = createStudent(); // Assuming you have a Student class
        StudentDto studentDto = createStudentDto(); // Assuming you have a StudentDto class
        when(studentRepository.getStudentByStudentCode(studentCode)).thenReturn(student);
        when(studentConverter.toDto(student)).thenReturn(studentDto);

        // When
        StudentDto result = studentService.viewStudentDetails(studentCode);

        // Then
        verify(studentRepository, times(1)).getStudentByStudentCode(studentCode);
        verify(studentConverter, times(1)).toDto(student);
        assertEquals(studentDto, result);
    }

    @Test
    public void testUpdateAttendingStatusOfStudents() {
        // Mocking data
        Long classId = 1L;
        List<Long> studentIds = Arrays.asList(101L, 102L);
        String attendingStatus = "Present";

        // Mocking behavior
        when(studentClassRepository.getStudentClassByStudentIdAndClassId(101L, classId))
                .thenReturn(new StudentClass());
        when(studentClassRepository.getStudentClassByStudentIdAndClassId(102L, classId))
                .thenReturn(new StudentClass());

        // Calling the method to be tested
        boolean result = studentClassService.updateAttendingStatusOfStudents(studentIds, classId, attendingStatus);

        // Verifying the result
        assertTrue(result);

        // Verifying that the save method was called twice
        verify(studentClassRepository, times(2)).save(any(StudentClass.class));
    }
    @Test
    public void testExportStudent() throws IOException {
        // Mock data
        List<Long> ids = new ArrayList<>();
        ids.add(1L);

        Student student = createStudent(); // Assuming you have a Student class
        StudentDto studentDto = createStudentDto(); // Assuming you have a StudentDto class

        when(studentRepository.getReferenceById(1L)).thenReturn(student);
        when(studentConverter.toDto(student)).thenReturn(studentDto);

        // Call the method
        byte[] result = studentService.exportStudent(ids);

        // Check result
        assertNotNull(result);

        // Check workbook content
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheetAt(0);

            // Check header row
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            assertEquals("Full name", headerRow.getCell(0).getStringCellValue());
            assertEquals("Date of birth", headerRow.getCell(1).getStringCellValue());
            assertEquals("Email", headerRow.getCell(2).getStringCellValue());
            assertEquals("Phone", headerRow.getCell(3).getStringCellValue());
            assertEquals("GPA", headerRow.getCell(4).getStringCellValue());
            assertEquals("RECer", headerRow.getCell(5).getStringCellValue());

            // Check data rows
            assertEquals(1, sheet.getLastRowNum()); // 1 rows of data
            Row row1 = sheet.getRow(1);
            assertNotNull(row1);
            assertEquals("John Doe", row1.getCell(0).getStringCellValue());
            assertEquals("2000-01-01", row1.getCell(1).getStringCellValue());
            assertEquals("john.doe@example.com", row1.getCell(2).getStringCellValue());
            assertEquals("1234567890", row1.getCell(3).getStringCellValue());
            assertEquals(3.799999952316284, row1.getCell(4).getNumericCellValue());
            assertEquals("Certificate XYZ", row1.getCell(5).getStringCellValue());
        }
    }

    @Test
    public void testViewListStudentByClass() {
        // Arrange
        Long classId = 1L;
        List<StudentClass> studentClassList = new ArrayList<>();
        StudentClass studentClass1 = new StudentClass();
        studentClass1.setId(1L);
        studentClass1.setStudent(new Student());
        studentClassList.add(studentClass1);

        when(studentClassRepository.getStudentClassByClassId(classId)).thenReturn(studentClassList);

        StudentDto studentDto = new StudentDto();
        // Assuming you properly configure studentConverter.toDto() to return studentDto
        when(studentConverter.toDto(any(Student.class))).thenReturn(studentDto);

        // Act
        List<StudentDto> result = studentService.viewListStudentByClass(classId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(studentDto, result.get(0));
    }

    @Test
    public void testViewListStudentByClassWithNullClassId() {
        // Arrange
        Long classId = null;

        // Act
        List<StudentDto> result = studentService.viewListStudentByClass(classId);

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    public void testEditInfoStudent_NotFound() {
        // Arrange
        long studentId = 1;
        StudentDto studentDto = new StudentDto();
        studentDto.setStatus("In Class");

        when(studentRepository.findById(anyLong())).thenReturn(java.util.Optional.empty());

        // Act
        StudentDto result = studentService.editInfoStudent(studentDto, studentId);

        // Assert
        assertEquals(null, result);
        verify(studentRepository, times(0)).save(any());
    }



    @Test
    public void testEditStudentInClass() {
        // Arrange
        StudentDto studentDTO = createStudentDto();
        Student student = createStudent();
        student.setStatus("In Class");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentConverter.toDto(any(Student.class))).thenReturn(studentDTO);

        // Act
        StudentDto result = studentService.editInfoStudent(studentDTO, 1L);

        // Assert
        assertNotNull(result);
        // Here you can add more specific assertions based on what changes are expected
    }
    @Test
    public void testEditStudentDropOut() {
        // Arrange
        StudentDto studentDTO = createStudentDto();
        studentDTO.setStatus("In Class");
        Student student = createStudent();
        student.setStatus("Drop Out");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentConverter.toDto(any(Student.class))).thenReturn(studentDTO);

        // Act
        StudentDto result = studentService.editInfoStudent(studentDTO, 1L);

        // Assert
        // Assert
        assertEquals("In Class", student.getStatus());
        // Here you can add more specific assertions based on what changes are expected
    }
    @Test
    public void testEditStudentReserve(){
        StudentDto studentDTO = createStudentDto();
        studentDTO.setStatus("reserve");
        Student student = createStudent();
        student.setStatus("reserve");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentConverter.toDto(any(Student.class))).thenReturn(studentDTO);

        // Act
        StudentDto result = studentService.editInfoStudent(studentDTO, 1L);

        // Assert
        // Assert
        assertEquals(result.getStatus(), "reserve");

    }

    @Test
    public void testEditStudentFinish(){
        StudentDto studentDTO = createStudentDto();
        studentDTO.setStatus("finish");
        Student student = createStudent();
        student.setStatus("finish");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentConverter.toDto(any(Student.class))).thenReturn(studentDTO);

        // Act
        StudentDto result = studentService.editInfoStudent(studentDTO, 1L);

        // Assert
        // Assert
        assertEquals(result.getStatus(), "finish");

    }
    @Test
    public void testEditInfoStudentDefault() {
        StudentDto studentDTO = createStudentDto();
        studentDTO.setStatus("active");
        Student student = createStudent();
        student.setStatus("active");

        // when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

//        when(studentConverter.toDto(student)).thenReturn(studentDTO);

        StudentDto result = studentService.editInfoStudent(studentDTO, 1L);

        // Ensure that the method returns null
        assertNull(result);
    }


    @Test
    public void testSaveStudentsFromCSV() throws IOException {

        // Prepare test data
        String csvData = "123 Main Street,Example Area,2000/01/01,john.doe@example.com,example_account,John Doe,Male,3.8,2022/05/15,2022/06/01,Computer Science,1234567890,Certificate XYZ,Example School,Active,ABC123,Type A\n";
        List<StudentDto> expectedStudentDTOList = new ArrayList<>();
        StudentDto studentDto = new StudentDto();
        studentDto.setAddress("123 Main Street");
        studentDto.setArea("Example Area");
        studentDto.setDob(LocalDate.of(2000, 1, 1));
        studentDto.setEmail("john.doe@example.com");
        studentDto.setFaAccount("example_account");
        studentDto.setFullName("John Doe");
        studentDto.setGender("Male");
        studentDto.setGpa(3.8f);
        studentDto.setGraduatedDate(LocalDate.of(2022, 5, 15));
        studentDto.setJoinedDate(LocalDate.of(2022, 6, 1));
        studentDto.setMajor("Computer Science");
        studentDto.setPhone("1234567890");
        studentDto.setReCer("Certificate XYZ");
        studentDto.setSchool("Example School");
        studentDto.setStatus("Active");
        studentDto.setStudentCode("ABC123");
        studentDto.setType("Type A");
        expectedStudentDTOList.add(studentDto);

        // Create a temporary CSV file with test data
        File tempFile = File.createTempFile("temp", ".csv");
        tempFile.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write(csvData);
        writer.close();

        // Call the method to be tested
        studentService.saveStudentsFromCSV(tempFile.getAbsolutePath());

        // Verify that save method of studentRepository is called for each student
        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository, times(expectedStudentDTOList.size())).save(studentCaptor.capture());

        // Assert that the captured students match the expected values
        List<Student> capturedStudents = studentCaptor.getAllValues();
        for (int i = 0; i < expectedStudentDTOList.size(); i++) {
            StudentDto expectedStudentDto = expectedStudentDTOList.get(i);
            Student capturedStudent = capturedStudents.get(i);
            // Compare the corresponding fields between StudentDTO and captured Student
            assertEquals(expectedStudentDto.getAddress(), capturedStudent.getAddress());
            assertEquals(expectedStudentDto.getArea(), capturedStudent.getArea());
            assertEquals(expectedStudentDto.getDob(), capturedStudent.getDob());
            assertEquals(expectedStudentDto.getEmail(), capturedStudent.getEmail());
            assertEquals(expectedStudentDto.getFaAccount(), capturedStudent.getFaAccount());
            assertEquals(expectedStudentDto.getFullName(), capturedStudent.getFullName());
            assertEquals(expectedStudentDto.getGender(), capturedStudent.getGender());
            assertEquals(expectedStudentDto.getGpa(), capturedStudent.getGpa());
            assertEquals(expectedStudentDto.getGraduatedDate(), capturedStudent.getGraduatedDate());
            assertEquals(expectedStudentDto.getJoinedDate(), capturedStudent.getJoinedDate());
            assertEquals(expectedStudentDto.getMajor(), capturedStudent.getMajor());
            assertEquals(expectedStudentDto.getPhone(), capturedStudent.getPhone());
            assertEquals(expectedStudentDto.getReCer(), capturedStudent.getReCer());
            assertEquals(expectedStudentDto.getSchool(), capturedStudent.getSchool());
            assertEquals(expectedStudentDto.getStatus(), capturedStudent.getStatus());
            assertEquals(expectedStudentDto.getStudentCode(), capturedStudent.getStudentCode());
            assertEquals(expectedStudentDto.getType(), capturedStudent.getType());
        }
    }
}
