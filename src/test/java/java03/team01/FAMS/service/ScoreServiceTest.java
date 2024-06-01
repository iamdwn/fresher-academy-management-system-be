package java03.team01.FAMS.service;

import java03.team01.FAMS.model.entity.Class;
import java03.team01.FAMS.model.entity.Module;
import java03.team01.FAMS.model.entity.*;
import java03.team01.FAMS.model.exception.FamsApiException;
import java03.team01.FAMS.model.exception.ResourceNotFoundException;
import java03.team01.FAMS.model.payload.requestModel.CustomCertificateRequest;
import java03.team01.FAMS.model.payload.requestModel.CustomScoreRequest;
import java03.team01.FAMS.model.payload.responseModel.*;
import java03.team01.FAMS.repository.*;
import java03.team01.FAMS.service.impl.ScoreServiceImpl;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ScoreServiceTest {
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private ModuleRepository moduleRepository;
    @Mock
    private ScoreRepository scoreRepository;
    @Mock
    private ClassRepository classRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StudentClassRepository studentClassRepository;
    @Mock
    private StudentModuleRepository studentModuleRepository;
    @InjectMocks
    private ScoreServiceImpl scoreService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }



    // TODO: Get Score Of Class
    @Test
    public void testGetScoreOfClass() {
        Long classId = 1L;
        int pageNo = 0;
        int pageSize = 10;

        Class clazz = new Class();
        clazz.setId(classId);

        List<Student> students = new ArrayList<>();
        Student student1 = new Student();
        student1.setId(1L);
        student1.setFullName("Student 1");
        student1.setFaAccount("account1");
        students.add(student1);

        List<Long> moduleIds = new ArrayList<>();
        moduleIds.add(1L);
        moduleIds.add(2L);

        when(classRepository.findClassById(classId)).thenReturn(Optional.of(clazz));
        when(studentClassRepository.getPageStudentByClassId(PageRequest.of(pageNo, pageSize), classId)).thenReturn(Page.empty());
        when(studentClassRepository.getStudentIdByClassId(classId)).thenReturn(List.of(1L));
        when(studentModuleRepository.getModuleByStudentId(1001L)).thenReturn(moduleIds);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));

        assertNotNull(scoreService.getScoreOfClass(pageNo, pageSize, classId));
        verify(classRepository, times(1)).findClassById(classId);
        verify(studentClassRepository, times(1)).getPageStudentByClassId(PageRequest.of(pageNo, pageSize), classId);
        verify(studentClassRepository, times(1)).getStudentIdByClassId(classId);
        verify(studentModuleRepository, times(1)).getModuleByStudentId(1L);
        verify(studentRepository, times(1)).findById(1L);
    }


    @Test
    public void testGetScoreOfClass_UnknownClass() {
        Long idClass = 1L;
        int pageNo = 0;
        int pageSize = 10;

        when(classRepository.findClassById(idClass)).thenReturn(Optional.empty());

        FamsApiException exception = assertThrows(FamsApiException.class,
                () -> scoreService.getScoreOfClass(pageNo, pageSize, idClass));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Unknown class", exception.getMessage());
    }

    @Test
    public void testGetScoreOfClass_WithStudentsInClass() {
        Long idClass = 1L;
        int pageNo = 0;
        int pageSize = 10;

        Class mockedClass = new Class();
        mockedClass.setId(idClass);

        Long studentId1 = 1L;
        Long studentId2 = 2L;
        List<Long> studentIds = Arrays.asList(studentId1, studentId2);

        Page<Object> mockedPage = mock(Page.class);

        Student student1 = new Student();
        student1.setId(studentId1);
        student1.setFullName("John Doe");
        student1.setFaAccount("john_doe");
        Student student2 = new Student();
        student2.setId(studentId2);
        student2.setFullName("Jane Doe");
        student2.setFaAccount("jane_doe");

        List<CustomScoreResponse> customScoreResponses = Arrays.asList(
                new CustomScoreResponse(student1.getId(), student1.getFullName(), student1.getFaAccount(), Collections.emptyList()),
                new CustomScoreResponse(student2.getId(), student2.getFullName(), student2.getFaAccount(), Collections.emptyList())
        );

        when(classRepository.findClassById(idClass)).thenReturn(Optional.of(mockedClass));
        when(studentClassRepository.getPageStudentByClassId(any(), eq(idClass))).thenReturn(mockedPage);
        when(studentClassRepository.getStudentIdByClassId(idClass)).thenReturn(studentIds);
        when(studentRepository.findById(studentId1)).thenReturn(Optional.of(student1));
        when(studentRepository.findById(studentId2)).thenReturn(Optional.of(student2));
        when(studentModuleRepository.getModuleByStudentId(studentId1)).thenReturn(Collections.emptyList());
        when(studentModuleRepository.getModuleByStudentId(studentId2)).thenReturn(Collections.emptyList());

        // Call the method and assert the response
        ScoreClassResponse scoreClassResponse = scoreService.getScoreOfClass(pageNo, pageSize, idClass);
        assertNotNull(scoreClassResponse);
        assertFalse(scoreClassResponse.getContent().isEmpty());
        assertEquals(2, scoreClassResponse.getContent().size());

        // Verify that the repository methods are called correctly
        verify(classRepository).findClassById(idClass);
        verify(studentClassRepository).getPageStudentByClassId(any(), eq(idClass));
        verify(studentClassRepository).getStudentIdByClassId(idClass);
        verify(studentRepository, times(2)).findById(anyLong());
        verify(studentModuleRepository, times(2)).getModuleByStudentId(anyLong());
        verifyNoInteractions(scoreRepository);
    }


    @Test
    public void testGetScoreOfClass_NoStudentsInClass() {
        Long idClass = 1L;
        int pageNo = 0;
        int pageSize = 10;

        Class mockedClass = new Class();
        mockedClass.setId(idClass);

        List<Long> studentIds = Collections.emptyList();
        Page<Object> mockedPage = mock(Page.class);

        when(classRepository.findClassById(idClass)).thenReturn(Optional.of(mockedClass));
        when(studentClassRepository.getPageStudentByClassId(any(), eq(idClass))).thenReturn(mockedPage);
        when(studentClassRepository.getStudentIdByClassId(idClass)).thenReturn(studentIds);

        ScoreClassResponse scoreClassResponse = scoreService.getScoreOfClass(pageNo, pageSize, idClass);
        assertNotNull(scoreClassResponse);
        assertTrue(scoreClassResponse.getContent().isEmpty());

        verify(classRepository).findClassById(idClass);
        verify(studentClassRepository).getPageStudentByClassId(any(), eq(idClass));
        verify(studentClassRepository).getStudentIdByClassId(idClass);
        verifyNoInteractions(studentRepository, studentModuleRepository, scoreRepository);
    }


    @Test
    public void getModule() {
        Long moduleId = 1L;
        List<Long> moduleIds = new ArrayList<>();
        moduleIds.add(1L);
        moduleIds.add(2L);
        Long studentId = 100L;

        when(studentModuleRepository.findByModuleIdAndStudentId(moduleId, studentId)).thenReturn(Optional.empty());

        List<CustomAssignResponse> result = scoreService.getAssignment(moduleId, studentId);

        assertEquals(0, result.size());
    }

    @Test
    public void testGetModule_UnknownStudentModule() {
        List<Long> moduleIds = List.of(1L, 2L);
        Long studentId = 1L;

        when(studentModuleRepository.findByModuleIdAndStudentId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(FamsApiException.class, () -> scoreService.getModule(moduleIds, studentId));

        assert(exception.getMessage().contains("Unknown student_module"));
    }

    @Test
    public void testGetModule_UnknownModule() {
        List<Long> moduleIds = List.of(1L, 2L);
        Long studentId = 1L;

        when(studentModuleRepository.findByModuleIdAndStudentId(anyLong(), anyLong())).thenReturn(Optional.of(new StudentModule()));
        //when(moduleRepository.findById(anyLong())).thenReturn(Optional.empty());

        FamsApiException exception = assertThrows(FamsApiException.class, () -> scoreService.getModule(moduleIds, studentId));

        assertEquals("404 NOT_FOUND", exception.getStatus().toString());
        assertTrue(exception.getMessage().contains("Student null missing a module"));
    }

    @Test
    public void testGetAssignment() {
        //Create mock data
        Long moduleId = 1L;
        Long studentId = 1L;
        Module module = new Module();
        Assignment assignment1 = new Assignment(1L, module, "Assignment 1", "Type 1",
                LocalDate.now(), "Description 1", LocalDate.now(), "User 1",
                LocalDate.now(), "User 1", new HashSet<>());
        Assignment assignment2 = new Assignment(2L, module, "Assignment 2", "Type 2",
                LocalDate.now(), "Description 2", LocalDate.now(), "User 2",
                LocalDate.now(), "User 2", new HashSet<>());

        List<Assignment> mockAssignments = new ArrayList<>();
        mockAssignments.add(assignment1);
        mockAssignments.add(assignment2);

        //Return assignments
        when(assignmentRepository.getListAssignmentByModuleId(moduleId)).thenReturn(mockAssignments);
        when(scoreRepository.getByAssignmentIdAndStudentId(1L, studentId)).thenReturn(Optional.empty());
        when(scoreRepository.getByAssignmentIdAndStudentId(2L, studentId)).thenReturn(Optional.empty());

        // Call method
        List<CustomAssignResponse> assignmentResponses = scoreService.getAssignment(moduleId, studentId);

        // Verify the assignment list = 2
        assertEquals(2, assignmentResponses.size());

        assertEquals(1L, assignmentResponses.get(0).getId());
        assertEquals("Type 1", assignmentResponses.get(0).getAssignType());
        assertEquals("Assignment 1", assignmentResponses.get(0).getAssignName());

        assertEquals(2L, assignmentResponses.get(1).getId());
        assertEquals("Type 2", assignmentResponses.get(1).getAssignType());
        assertEquals("Assignment 2", assignmentResponses.get(1).getAssignName());
    }

    @Test
    public void testGetAverageModuleScore_WithNoModules() {
        CustomScoreResponse customScoreResponse = new CustomScoreResponse();
        //No create module so = 0.0
        assertEquals(0.0, customScoreResponse.getAverageModuleScore());
    }

    @Test
    public void testGetAverageModuleScore_WithModules() {
        CustomScoreResponse customScoreResponse = new CustomScoreResponse();
        //ensure return list module not null
        customScoreResponse.setModule(new ArrayList<>());
        CustomModuleResponse module1 = new CustomModuleResponse();
        module1.setModuleScore(8.0f);
        CustomModuleResponse module2 = new CustomModuleResponse();
        module2.setModuleScore(7.5f);

        customScoreResponse.getModule().add(module1);
        customScoreResponse.getModule().add(module2);

        double expectedAverage = (8.0f + 7.5f) / 2f;

        assertEquals(expectedAverage, customScoreResponse.getAverageModuleScore());
    }







    // TODO : TEst Get Score By Student Code
    @Test
    public void testGetScoreOfClassAndStudentCode() {
        Long idClass = 1L;
        String studentCode = "SE123";
        Long studentId = 1L;

        Class classEntity = new Class();
        classEntity.setClassCode("CS101");
        classEntity.setDuration(Duration.ofDays(8));

        Student studentEntity = new Student();
        studentEntity.setId(studentId);
        studentEntity.setFullName("vu khai");
        studentEntity.setFaAccount("vk123");

        List<Long> moduleIds = Arrays.asList(1L, 2L);

        StudentClass studentClassEntity = new StudentClass();
        studentClassEntity.setFinalScore(90.0F);
        studentClassEntity.setGpaLevel("High");

        Module moduleEntity = new Module();
        moduleEntity.setModuleName("OJT");
        moduleEntity.setId(1L);

        StudentModule studentModule = new StudentModule();
        studentModule.setStudent(studentEntity);
        studentModule.setModule(moduleEntity);

        when(studentModuleRepository.findByModuleIdAndStudentId(anyLong(), anyLong())).thenReturn(Optional.of(studentModule));
        when(moduleRepository.findById(anyLong())).thenReturn(Optional.of(moduleEntity));
        when(classRepository.findClassById(idClass)).thenReturn(Optional.of(classEntity));
        when(studentRepository.getStudentByStudentCode(studentCode)).thenReturn(studentEntity);
        when(studentClassRepository.getStudentIdByStudentCode(anyLong(), anyLong())).thenReturn(studentId);
        when(studentModuleRepository.getModuleByStudentId(studentId)).thenReturn(moduleIds);
        when(studentClassRepository.getStudentClassByStudentId(studentId, idClass)).thenReturn(Optional.of(studentClassEntity));
        //when(scoreService.getModule(moduleIds, studentId)).thenReturn(null);

        CustomObjectResponse response = scoreService.getScoreOfClassAndStudentCode(idClass, studentCode);

        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getData());
    }

    @Test
    public void testGetScoreOfClassAndStudentCode_ClassNotFound() {
        Long idClass = 1L;
        String studentCode = "SE123";

        when(classRepository.findClassById(idClass)).thenReturn(Optional.empty());

        CustomObjectResponse response = scoreService.getScoreOfClassAndStudentCode(idClass, studentCode);

        assertEquals("BAD_REQUEST", response.getStatus());
        assertTrue(response.getMessage().contains("Not found this class"));
    }

    @Test
    public void testGetScoreOfClassAndStudentCode_StudentNotFound() {
        Long idClass = 1L;
        String studentCode = "SE123";

        when(classRepository.findClassById(idClass)).thenReturn(Optional.of(new Class()));
        when(studentRepository.getStudentByStudentCode(studentCode)).thenReturn(null);

        CustomObjectResponse response = scoreService.getScoreOfClassAndStudentCode(idClass, studentCode);

        assertEquals("BAD_REQUEST", response.getStatus());
        assertTrue(response.getMessage().contains("Not found this student"));
    }





    // TODO : Test Update Score For Student
    @Test
    public void testUpdateScoreOfClass_Success() {
        CustomScoreRequest customScoreRequest = new CustomScoreRequest();
        customScoreRequest.setScoreId(1L);
        customScoreRequest.setScoreValue(9.5F);

        Long studentId = 1L;
        Long moduleId = 2L;
        Long assignmentId = 3L;

        Score score = new Score();
        score.setId(customScoreRequest.getScoreId());

        Student student = new Student();
        student.setId(studentId);

        StudentModule studentModule = new StudentModule();
        studentModule.setModuleScore(8.5F);

        Assignment assignment1 = new Assignment();
        assignment1.setAssignmentName("OJT");

        List<Assignment> assignments = new ArrayList<>();
        assignments.add(assignment1);


        when(scoreRepository.getStudentIdByScoreId(customScoreRequest.getScoreId())).thenReturn(studentId);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(scoreRepository.getAssignmentIdByScoreId(customScoreRequest.getScoreId())).thenReturn(assignmentId);
        when(assignmentRepository.getModuleIdByAssignmentId(assignmentId)).thenReturn(moduleId);
        when(studentModuleRepository.findByModuleIdAndStudentId(moduleId, studentId)).thenReturn(Optional.of(studentModule));
        when(assignmentRepository.getAssignmentByModuleIdAndStudentId(moduleId, studentId)).thenReturn(assignments);
        when(scoreRepository.getScoreAssignmentIdAndStudentId(assignmentId, studentId, customScoreRequest.getScoreId())).thenReturn(Optional.of(score));

        CustomObjectResponse response = scoreService.updateScoreOfClass(customScoreRequest);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals("The scores have been successfully updated", response.getMessage());
    }

    @Test
    public void testUpdateScoreOfClass_ModuleNotFound() {
        CustomScoreRequest customScoreRequest = new CustomScoreRequest();
        customScoreRequest.setScoreId(1L);
        customScoreRequest.setScoreValue(9.5F);

        Long studentId = 1L;
        Long assignmentId = 3L;

        when(scoreRepository.getStudentIdByScoreId(customScoreRequest.getScoreId())).thenReturn(studentId);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(new Student()));
        when(scoreRepository.getAssignmentIdByScoreId(customScoreRequest.getScoreId())).thenReturn(assignmentId);
        when(assignmentRepository.getModuleIdByAssignmentId(assignmentId)).thenReturn(null);

        CustomObjectResponse response = scoreService.updateScoreOfClass(customScoreRequest);

        assertNotNull(response);
        assertEquals("BAD_REQUEST", response.getStatus());
        assertEquals("Not found any module scores of this student", response.getMessage());
        assertEquals("Updated failed", response.getData());
    }

    @Test
    public void testUpdateScoreOfClass_StudentNotFound() {
        CustomScoreRequest customScoreRequest = new CustomScoreRequest();
        customScoreRequest.setScoreId(1L);
        customScoreRequest.setScoreValue(9.5F);

        Long studentId = 1L;

        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        when(scoreRepository.getStudentIdByScoreId(customScoreRequest.getScoreId())).thenReturn(studentId);
        when(assignmentRepository.getModuleIdByAssignmentId(anyLong())).thenReturn(1L);
        when(studentModuleRepository.findByModuleIdAndStudentId(anyLong(), anyLong())).thenReturn(Optional.of(new StudentModule()));
        when(assignmentRepository.getAssignmentByModuleIdAndStudentId(anyLong(), anyLong())).thenReturn(Collections.singletonList(new Assignment()));
        when(scoreRepository.getScoreAssignmentIdAndStudentId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.of(new Score()));

        CustomObjectResponse response = scoreService.updateScoreOfClass(customScoreRequest);

        assertNotNull(response);
        assertEquals("BAD_REQUEST", response.getStatus());
        assertEquals("Not found this student", response.getMessage());
        assertEquals("Updated failed", response.getData());
    }


    @Test
    public void testUpdateScoreOfClass_AssignmentsNotFound() {
        CustomScoreRequest customScoreRequest = new CustomScoreRequest();
        customScoreRequest.setScoreId(1L);
        customScoreRequest.setScoreValue(9.5F);

        Long studentId = 1L;

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(new Student()));
        when(assignmentRepository.getAssignmentByModuleIdAndStudentId(anyLong(), anyLong())).thenReturn(Collections.emptyList());

        when(scoreRepository.getStudentIdByScoreId(customScoreRequest.getScoreId())).thenReturn(studentId);
        when(assignmentRepository.getModuleIdByAssignmentId(anyLong())).thenReturn(1L);
        when(studentModuleRepository.findByModuleIdAndStudentId(anyLong(), anyLong())).thenReturn(Optional.of(new StudentModule()));
        when(scoreRepository.getScoreAssignmentIdAndStudentId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.of(new Score()));

        CustomObjectResponse response = scoreService.updateScoreOfClass(customScoreRequest);

        assertNotNull(response);
        assertEquals("BAD_REQUEST", response.getStatus());
        assertEquals("Not found any assignments", response.getMessage());
        assertEquals("Updated failed", response.getData());
    }

    @Test
    public void testUpdateScoreOfClass_ScoresNotFound() {
        CustomScoreRequest customScoreRequest = new CustomScoreRequest();
        customScoreRequest.setScoreId(1L);
        customScoreRequest.setScoreValue(9.5F);

        Long studentId = 1L;
        Long assignmentId = 1L;
        //Long moduleId = 1L;

        when(scoreRepository.getStudentIdByScoreId(customScoreRequest.getScoreId())).thenReturn(studentId);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(new Student()));
        when(assignmentRepository.getModuleIdByAssignmentId(anyLong())).thenReturn(1L);
        when(studentModuleRepository.findByModuleIdAndStudentId(anyLong(), anyLong())).thenReturn(Optional.of(new StudentModule()));
        when(assignmentRepository.getAssignmentByModuleIdAndStudentId(anyLong(), anyLong())).thenReturn(Collections.singletonList(new Assignment()));

        when(scoreRepository.getScoreAssignmentIdAndStudentId(assignmentId, studentId, customScoreRequest.getScoreId())).thenReturn(Optional.empty());

        CustomObjectResponse response = scoreService.updateScoreOfClass(customScoreRequest);

        assertNotNull(response);
        assertEquals("BAD_REQUEST", response.getStatus());
        assertEquals("Not found any scores of this student", response.getMessage());
        assertEquals("Updated failed", response.getData());
    }

    @Test
    public void testUpdateScoreOfClass_InvalidScore() {
        CustomScoreRequest customScoreRequest = new CustomScoreRequest();
        customScoreRequest.setScoreId(3L);
        customScoreRequest.setScoreValue(15F);

        Long studentId = 1L;

        when(scoreRepository.getStudentIdByScoreId(customScoreRequest.getScoreId())).thenReturn(studentId);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(new Student()));
        when(assignmentRepository.getModuleIdByAssignmentId(anyLong())).thenReturn(1L);
        when(studentModuleRepository.findByModuleIdAndStudentId(anyLong(), anyLong())).thenReturn(Optional.of(new StudentModule()));
        when(assignmentRepository.getAssignmentByModuleIdAndStudentId(anyLong(), anyLong())).thenReturn(Collections.singletonList(new Assignment()));
        when(scoreRepository.getScoreAssignmentIdAndStudentId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.of(new Score()));

        CustomObjectResponse response = scoreService.updateScoreOfClass(customScoreRequest);

        assertNotNull(response);
        assertEquals("BAD_REQUEST", response.getStatus());
        assertEquals("Value of this score is out of range", response.getMessage());
        assertEquals("Updated failed", response.getData());
    }




    // TODO : Test Update Certificate

    @Test
    public void testUpdateCertificate() {
        CustomCertificateRequest customCertificateRequest = new CustomCertificateRequest();
        customCertificateRequest.setStudentId(1L);
        customCertificateRequest.setCertificateStatus("Approved");
        customCertificateRequest.setCertificateDate(new Date());

        Long moduleId = 1L;
        Module module = new Module();
        module.setId(1L);
        module.setModuleName("OJT");

        List<StudentModule> studentModules = new ArrayList<>();
        StudentModule studentModule = new StudentModule();
        studentModule.setModuleScore(80F);
        studentModule.setModule(module);
        studentModule.setStatus("PASSED");
        studentModules.add(studentModule);
        StudentModule studentModule2 = new StudentModule();
        studentModule2.setModuleScore(90F);
        studentModule2.setModule(module);
        studentModule2.setStatus("PASSED");
        studentModules.add(studentModule2);

        Set<Score> scores = new HashSet<>();
        Score score = new Score();
        score.setScore(10F);
        scores.add(score);

        List<Assignment> assignments = new ArrayList<>();
        Assignment assignment = new Assignment();
        assignment.setScores(scores);
        assignments.add(assignment);

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(new Student()));
        when(studentModuleRepository.findByStudentId(customCertificateRequest.getStudentId())).thenReturn(studentModules);
        when(assignmentRepository.getAssignmentByModuleIdAndStudentId(moduleId, customCertificateRequest.getStudentId()))
                .thenReturn(assignments);

        CustomObjectResponse response = scoreService.updateCertificate(customCertificateRequest);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals("This student is qualified to receive a certificate", response.getMessage());
        assertEquals("Updated successfully", response.getData());
    }

    @Test
    public void testUpdateCertificate_StudentNotFound() {
        CustomCertificateRequest customCertificateRequest = new CustomCertificateRequest();
        customCertificateRequest.setStudentId(999L);

        when(studentRepository.findById(customCertificateRequest.getStudentId())).thenReturn(Optional.empty());

        CustomObjectResponse response = scoreService.updateCertificate(customCertificateRequest);

        assertEquals("BAD_REQUEST", response.getStatus());
        assertEquals("Not found this student", response.getMessage());
        assertEquals("Updated failed", response.getData());
    }

    @Test
    public void testUpdateCertificate_ModuleNullForStudent() {
        CustomCertificateRequest customCertificateRequest = new CustomCertificateRequest();
        customCertificateRequest.setStudentId(1L);
        customCertificateRequest.setCertificateStatus("Approved");
        customCertificateRequest.setCertificateDate(new Date());

        Long moduleId = 1L;

        List<StudentModule> studentModules = new ArrayList<>();
        StudentModule studentModule = new StudentModule();
        studentModule.setModuleScore(80F);
        studentModules.add(studentModule);
        StudentModule studentModule2 = new StudentModule();
        studentModule2.setModuleScore(90F);
        studentModules.add(studentModule2);

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(new Student()));
        when(studentModuleRepository.findByStudentId(customCertificateRequest.getStudentId())).thenReturn(studentModules);
        //when(assignmentRepository.getAssignmentByModuleIdAndStudentId(moduleId, customCertificateRequest.getStudentId()))
                //.thenReturn(assignments);

        CustomObjectResponse response = scoreService.updateCertificate(customCertificateRequest);

        assertEquals("BAD_REQUEST", response.getStatus());
        assertEquals("Not found any modules of this student", response.getMessage());
        assertEquals("Updated failed", response.getData());
    }
    @Test
    public void testUpdateCertificate_ModulePointsNotFull() {
        CustomCertificateRequest customCertificateRequest = new CustomCertificateRequest();
        customCertificateRequest.setStudentId(1L);
        customCertificateRequest.setCertificateStatus("Approved");
        customCertificateRequest.setCertificateDate(new Date());

        List<StudentModule> studentModules = new ArrayList<>();

        StudentModule studentModule = new StudentModule();
        studentModule.setModuleScore(Float.NaN); // score is not a number
        studentModules.add(studentModule);

        StudentModule studentModule2 = new StudentModule();
        studentModule2.setModuleScore(0F);
        studentModules.add(studentModule2);

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(new Student()));
        when(studentModuleRepository.findByStudentId(customCertificateRequest.getStudentId()))
                .thenReturn(studentModules);

        CustomObjectResponse response = scoreService.updateCertificate(customCertificateRequest);

        assertEquals("BAD_REQUEST", response.getStatus());
        assertEquals("This student does not have full module points", response.getMessage());
        assertEquals("Updated failed", response.getData());
    }

    @Test
    public void testUpdateCertificate_AssignmentPointsNotFull() {
        CustomCertificateRequest customCertificateRequest = new CustomCertificateRequest();
        customCertificateRequest.setStudentId(1L);
        customCertificateRequest.setCertificateStatus("Approved");
        customCertificateRequest.setCertificateDate(new Date());

        Long moduleId = 1L;
        Module module = new Module();
        module.setId(1L);
        module.setModuleName("OJT");

        List<StudentModule> studentModules = new ArrayList<>();
        StudentModule studentModule = new StudentModule();
        studentModule.setModuleScore(80F);
        studentModule.setModule(module);
        studentModules.add(studentModule);
        StudentModule studentModule2 = new StudentModule();
        studentModule2.setModuleScore(90F);
        studentModule2.setModule(module);
        studentModules.add(studentModule2);

        Set<Score> scores = new HashSet<>();
        List<Assignment> assignments = new ArrayList<>();
        Assignment assignment = new Assignment();
        assignment.setScores(scores);
        assignments.add(assignment);

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(new Student()));
        when(studentModuleRepository.findByStudentId(customCertificateRequest.getStudentId())).thenReturn(studentModules);
        when(assignmentRepository.getAssignmentByModuleIdAndStudentId(moduleId, customCertificateRequest.getStudentId()))
                .thenReturn(assignments);

        CustomObjectResponse response = scoreService.updateCertificate(customCertificateRequest);

        assertEquals("BAD_REQUEST", response.getStatus());
        assertEquals("This student does not have full assignment points", response.getMessage());
        assertEquals("Updated failed", response.getData());
    }

    @Test
    public void testUpdateCertificate_StudentNotFinishModule() {
        CustomCertificateRequest customCertificateRequest = new CustomCertificateRequest();
        customCertificateRequest.setStudentId(1L);
        customCertificateRequest.setCertificateStatus("Approved");
        customCertificateRequest.setCertificateDate(new Date());

        Long moduleId = 1L;
        Module module = new Module();
        module.setId(1L);
        module.setModuleName("OJT");

        List<StudentModule> studentModules = new ArrayList<>();
        StudentModule studentModule = new StudentModule();
        studentModule.setModuleScore(80F);
        studentModule.setModule(module);
        studentModule.setStatus("hihi lazada");
        studentModules.add(studentModule);
        StudentModule studentModule2 = new StudentModule();
        studentModule2.setModuleScore(90F);
        studentModule2.setModule(module);
        studentModule2.setStatus("hihi lazada");
        studentModules.add(studentModule2);

        Set<Score> scores = new HashSet<>();
        Score score = new Score();
        score.setScore(10F);
        scores.add(score);

        List<Assignment> assignments = new ArrayList<>();
        Assignment assignment = new Assignment();
        assignment.setScores(scores);
        assignments.add(assignment);

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(new Student()));
        when(studentModuleRepository.findByStudentId(customCertificateRequest.getStudentId())).thenReturn(studentModules);
        when(assignmentRepository.getAssignmentByModuleIdAndStudentId(moduleId, customCertificateRequest.getStudentId()))
                .thenReturn(assignments);

        CustomObjectResponse response = scoreService.updateCertificate(customCertificateRequest);

        assertEquals("BAD_REQUEST", response.getStatus());
        assertEquals("This student does not have passed all modules", response.getMessage());
        assertEquals("Updated failed", response.getData());
    }





    // TODO : Test Import Score From File


    @Test
    public void testImportScoresFromExcel_Fail() throws IOException {
        File file = new File("test-import.xlsx");
        String fieldName = "name";
        Long fieldValue = 1L;

        when(assignmentRepository.findById(anyLong())).thenThrow(new ResourceNotFoundException("Assignment not found", fieldName, fieldValue));
        when(studentRepository.findById(anyLong())).thenThrow(new ResourceNotFoundException("Student not found", fieldName, fieldValue));

        try {
            scoreService.importScoresFromExcel(file);
        } catch (FamsApiException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals("Import fail!", e.getMessage());
        }
    }

    @Test
    public void testInvalidScoreFormat() throws IOException, OpenXML4JException {
        MultipartFile multipartFile = mock(MultipartFile.class);

        when(multipartFile.isEmpty()).thenReturn(false);

        File tempFile = createTempFileWithInvalidScore();

        AssignmentRepository assignmentRepository = mock(AssignmentRepository.class);
        StudentRepository studentRepository = mock(StudentRepository.class);
        ModuleRepository moduleRepository = mock(ModuleRepository.class);
        StudentModuleRepository studentModuleRepository = mock(StudentModuleRepository.class);
        ScoreRepository scoreRepository = mock(ScoreRepository.class);

        assertThrows(NullPointerException.class, () -> scoreService.uploadFile(multipartFile));
        tempFile.delete();
    }
    private File createTempFileWithInvalidScore() throws IOException {
        File tempFile = File.createTempFile("test-import", ".xlsx");
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(1L);
        row.createCell(1).setCellValue(1L);
        row.createCell(2).setCellValue(11); // Invalid score value
        row.createCell(3).setCellValue("2023-01-02");
        row.createCell(4).setCellValue(1L);
        FileOutputStream fos = new FileOutputStream(tempFile);
        workbook.write(fos);
        fos.close();
        workbook.close();
        return tempFile;
    }

    @Test
    void whenImportFileNotExcelFile_ThrowException() {
        String content = "1,2024-11-06,2024-05-06,1,1,thich,active";
        MockMultipartFile file = new MockMultipartFile(
                "reservation_class_file",
                "file.txt",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                content.getBytes());

        FamsApiException famsApiException = Assertions.assertThrows(
                FamsApiException.class, () -> scoreService.uploadFile(file));

        assertEquals(HttpStatus.BAD_REQUEST, famsApiException.getStatus());
        assertEquals("Only .xlsx file is supported!", famsApiException.getMessage());


    }

    // TODO : Test Generate Score File

    @Test
    public void testGenerateExcelFromScores() throws IOException {
        Student student = new Student();
        student.setId(1L);
        student.setStudentCode("SE123");

        Assignment assignment = new Assignment();
        assignment.setId(1L);
        assignment.setAssignmentName("OJT");

        Float score = 1F;
        Float score5 = 2F;

        List<Score> scoreList = new ArrayList<>();
        Score score1 = new Score(1L, student, assignment, score, LocalDate.of(2024, 3, 28));
        Score score2 = new Score(2L, student, assignment, score5, LocalDate.of(2024, 3, 29));
        scoreList.add(score1);
        scoreList.add(score2);

        ByteArrayInputStream excelStream = scoreService.generateExcelFromScores(scoreList);

        assertNotNull(excelStream);

        XSSFWorkbook workbook = new XSSFWorkbook(excelStream);
        assertNotNull(workbook);

        assertEquals("Scores", workbook.getSheetName(0));

        Row headerRow = workbook.getSheetAt(0).getRow(0);
        assertEquals("score_id", headerRow.getCell(0).getStringCellValue());
        assertEquals("score", headerRow.getCell(1).getStringCellValue());
        assertEquals("submission_date", headerRow.getCell(2).getStringCellValue());
        assertEquals("assignment_id", headerRow.getCell(3).getStringCellValue());
        assertEquals("student_id", headerRow.getCell(4).getStringCellValue());

        for (int i = 0; i < scoreList.size(); i++) {
            Row row = workbook.getSheetAt(0).getRow(i + 1);
            assertNotNull(row);
            assertEquals(scoreList.get(i).getId(), (long) row.getCell(0).getNumericCellValue());
            assertEquals(scoreList.get(i).getScore(), (float) row.getCell(1).getNumericCellValue(), 0.01f);
            assertEquals(scoreList.get(i).getSubmissionDate().toString(), row.getCell(2).getStringCellValue());
            assertEquals(scoreList.get(i).getAssignment().getId(), (long) row.getCell(3).getNumericCellValue());
            assertEquals(scoreList.get(i).getStudent().getId(), (long) row.getCell(4).getNumericCellValue());
        }

        workbook.close();
    }

    @Test
    public void testGenerateExcelFromScores_Fail() throws IOException {
        Student student = new Student();
        student.setId(1L);
        student.setStudentCode("SE123");

        Assignment assignment = new Assignment();
        assignment.setId(1L);
        assignment.setAssignmentName("OJT");

        Float score = 1F;
        Float score5 = 2F;

        List<Score> scoreList = new ArrayList<>();
        Score score1 = new Score(1L, student, assignment, score, LocalDate.of(2024, 3, 28));
        Score score2 = new Score(2L, student, assignment, score5, LocalDate.of(2024, 3, 29));
        scoreList.add(score1);
        scoreList.add(score2);

        assertThrows(RuntimeException.class, () -> {
            // Tạo một ByteArrayOutputStream để lưu trữ dữ liệu của file Excel
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream() {
                @Override
                public void write(byte[] b, int off, int len) {
                    throw new RuntimeException("Failed to write data");
                }
            };

            // Mock Workbook.write() để ném RuntimeException
            try (Workbook workbook = new XSSFWorkbook()) {
                workbook.createSheet("Scores");
                workbook.write(outputStream); // Ghi dữ liệu vào outputStream
            }
        });
    }

    @Test
    public void testGenerateTemplateExcelFromScores_Fail() throws IOException {
        assertThrows(RuntimeException.class, () -> {
            // Tạo một ByteArrayOutputStream để lưu trữ dữ liệu của file Excel
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream() {
                @Override
                public void write(byte[] b, int off, int len) {
                    throw new RuntimeException("Failed to write data");
                }
            };

            // Mock Workbook.write() để ném RuntimeException
            try (Workbook workbook = new XSSFWorkbook()) {
                workbook.createSheet("Scores");
                workbook.write(outputStream); // Ghi dữ liệu vào outputStream
            }
        });
    }
}
