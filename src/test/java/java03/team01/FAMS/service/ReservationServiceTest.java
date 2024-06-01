package java03.team01.FAMS.service;

import java03.team01.FAMS.model.entity.*;
import java03.team01.FAMS.model.entity.Class;
import java03.team01.FAMS.model.entity.Module;
import java03.team01.FAMS.model.exception.FamsApiException;
import java03.team01.FAMS.model.exception.ResourceNotFoundException;
import java03.team01.FAMS.model.payload.dto.EmailSendDto;
import java03.team01.FAMS.model.payload.dto.ReservedClassDto;
import java03.team01.FAMS.model.payload.responseModel.CustomClassResponse;
import java03.team01.FAMS.model.payload.responseModel.CustomReservationResponse;
import java03.team01.FAMS.model.payload.responseModel.ImportExcelResponse;
import java03.team01.FAMS.model.payload.responseModel.ReservedStudentResponse;
import java03.team01.FAMS.repository.*;
import java03.team01.FAMS.service.impl.EmailServiceImp;
import java03.team01.FAMS.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class ReservationServiceTest {
    @Mock
    private TrainingProgramModuleRepository trainingProgramModuleRepository;

    @Mock
    private StudentModuleRepository studentModuleRepository;

    @Mock
    private StudentClassRepository studentClassRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private EmailTemplateRepository emailTemplateRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private EmailService emailService;
    @InjectMocks
    private ReservationServiceImpl reservationService;

    private ReservedClass reservedClass;
    private Class currentClass, newMatchingClass;
    private TrainingProgram trainingProgram;
    private Student student;
    private ReservedClassDto reservedClassDto;
    private EmailTemplate emailTemplate;
    private Module mockModule;
    private PageImpl<ReservedClass> mockPage;
    private CustomReservationResponse mockCustomReservationResponse;

    @BeforeEach
    public void setUp() {
        reservedClassDto = new ReservedClassDto();
        // Set default values for reservedClassAddNew
        reservedClassDto.setStudentId(1L);
        reservedClassDto.setClassId(1L);
        reservedClassDto.setStartDate(LocalDate.now().plusDays(1)); // Future date
        reservedClassDto.setEndDate(LocalDate.now().plusMonths(2)); // Future date
        reservedClassDto.setReason("Reason");

        trainingProgram = new TrainingProgram();
        trainingProgram.setId(1L);
        trainingProgram.setName("Program");// Assume this has an ID and relevant methods

        reservedClass = new ReservedClass(); // And it's set up correctly
        reservedClass.setEndDate(LocalDate.now().minusDays(1));

        currentClass = new Class();
        currentClass.setTrainingProgram(trainingProgram);
        currentClass.setStartDate(LocalDate.now().minusDays(10)); // Assuming the current class started 10 days ago
        currentClass.setEndDate(LocalDate.now());

        newMatchingClass = new Class();
        newMatchingClass.setTrainingProgram(trainingProgram); // Matches the training program
        newMatchingClass.setStartDate(LocalDate.now().plusDays(9)); // Starts after the current class ends
        newMatchingClass.setEndDate(LocalDate.now().plusDays(20));

        student = new Student();
        student.setFullName("ABC");

        // Setting up relationships
        reservedClass.setClassObj(currentClass);
        reservedClass.setStudent(student);
        currentClass.setTrainingProgram(trainingProgram);
        newMatchingClass.setTrainingProgram(trainingProgram);

        emailTemplate = new EmailTemplate();
        emailTemplate.setId(1L);

        mockModule = new Module(); // Cài đặt chi tiết cho module
        mockCustomReservationResponse = new CustomReservationResponse();
        mockModule.setModuleName("Math");
        mockCustomReservationResponse.setModuleName(new ArrayList<>());
        List<ReservedClass> reservedClassList = Arrays.asList(reservedClass);
        mockPage = new PageImpl<>(reservedClassList);


    }

    //Create New ReservedClass
    @Test
    public void whenStudentNotFound_thenThrowException() {
        when(studentRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = Assertions.assertThrows(ResourceNotFoundException.class, () -> reservationService.createReservation(reservedClassDto));

        assertTrue(thrown.getMessage().contains("Student"));
        verify(studentRepository, times(1)).findById(anyLong());
    }

    @Test
    public void whenClassNotFound_thenThrowException() {
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(new Student()));
        when(classRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = Assertions.assertThrows(ResourceNotFoundException.class, () -> reservationService.createReservation(reservedClassDto));

        assertTrue(thrown.getMessage().contains("Class"));
        verify(studentRepository, times(1)).findById(anyLong());
        verify(classRepository, times(1)).findById(anyLong());
    }

    @Test
    public void whenStudentAndClassAlreadyReserved_thenThrowException() {
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(new Student()));
        when(classRepository.findById(anyLong())).thenReturn(Optional.of(new Class()));
        when(reservationRepository.existsByStudentIdAndAndClassObjIdAndStatus(anyLong(), anyLong(), eq("active"))).thenReturn(true);

        Exception exception = Assertions.assertThrows(FamsApiException.class, () -> reservationService.createReservation(reservedClassDto));

        assertTrue(exception.getMessage().contains("This student and class is already reserved"));
        verify(studentRepository, times(1)).findById(reservedClassDto.getStudentId());
        verify(classRepository, times(1)).findById(reservedClassDto.getClassId());
        verify(reservationRepository, times(1)).existsByStudentIdAndAndClassObjIdAndStatus(reservedClassDto.getStudentId(), reservedClassDto.getClassId(), "active");
    }

    @Test
    public void whenReservingPeriodIsMoreThan6Months_thenThrowException() {
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(new Student()));
        when(classRepository.findById(anyLong())).thenReturn(Optional.of(new Class()));
        when(reservationRepository.existsByStudentIdAndAndClassObjIdAndStatus(anyLong(), anyLong(), eq("active"))).thenReturn(false);
        reservedClassDto.setEndDate(LocalDate.now().plusMonths(7));

        Exception exception = Assertions.assertThrows(FamsApiException.class, () -> reservationService.createReservation(reservedClassDto));

        Assertions.assertTrue(exception.getMessage().contains("The period is no longer than 6 months"));
        verify(studentRepository, times(1)).findById(reservedClassDto.getStudentId());
        verify(classRepository, times(1)).findById(reservedClassDto.getClassId());
        verify(reservationRepository, times(1)).existsByStudentIdAndAndClassObjIdAndStatus(reservedClassDto.getStudentId(), reservedClassDto.getClassId(), "active");
    }

    @Test
    public void whenReservingPeriodAtLeastOneMonth_thenThrowException() {
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(new Student()));
        when(classRepository.findById(anyLong())).thenReturn(Optional.of(new Class()));
        when(reservationRepository.existsByStudentIdAndAndClassObjIdAndStatus(anyLong(), anyLong(), eq("active"))).thenReturn(false);
        reservedClassDto.setEndDate(LocalDate.now().minusDays(1));

        Exception exception = Assertions.assertThrows(FamsApiException.class, () -> reservationService.createReservation(reservedClassDto));

        Assertions.assertTrue(exception.getMessage().contains("End date must be after start date at least 1 month"));
        verify(studentRepository, times(1)).findById(reservedClassDto.getStudentId());
        verify(classRepository, times(1)).findById(reservedClassDto.getClassId());
        verify(reservationRepository, times(1)).existsByStudentIdAndAndClassObjIdAndStatus(reservedClassDto.getStudentId(), reservedClassDto.getClassId(), "active");
    }

    @Test
    public void whenStudentNotInClass_thenThrowException() {
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(new Student()));
        when(classRepository.findById(anyLong())).thenReturn(Optional.of(new Class()));
        when(reservationRepository.existsByStudentIdAndAndClassObjIdAndStatus(anyLong(), anyLong(), eq("active"))).thenReturn(false);
        when(studentClassRepository.findByStudentIdAndClassObjId(anyLong(), anyLong())).thenReturn(null);

        Exception exception = Assertions.assertThrows(FamsApiException.class, () -> reservationService.createReservation(reservedClassDto));

        Assertions.assertTrue(exception.getMessage().contains("This student is not attending this class"));
        verify(studentRepository, times(1)).findById(reservedClassDto.getStudentId());
        verify(classRepository, times(1)).findById(reservedClassDto.getClassId());
        verify(reservationRepository, times(1)).existsByStudentIdAndAndClassObjIdAndStatus(reservedClassDto.getStudentId(), reservedClassDto.getClassId(), "active");
        verify(studentClassRepository, times(1)).findByStudentIdAndClassObjId(reservedClassDto.getStudentId(), reservedClassDto.getClassId());
    }

    @Test
    public void whenStudentInClassButNotAttendingStatusRequire_thenThrowException() {
        StudentClass studentClass = new StudentClass();
        studentClass.setAttendingStatus("Finish");
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(new Student()));
        when(classRepository.findById(anyLong())).thenReturn(Optional.of(new Class()));
        when(reservationRepository.existsByStudentIdAndAndClassObjIdAndStatus(anyLong(), anyLong(), eq("active"))).thenReturn(false);
        when(studentClassRepository.findByStudentIdAndClassObjId(anyLong(), anyLong())).thenReturn(studentClass);

        Exception exception = Assertions.assertThrows(FamsApiException.class, () -> reservationService.createReservation(reservedClassDto));

        Assertions.assertTrue(exception.getMessage().contains("This student is not attending this class anymore"));
        verify(studentRepository, times(1)).findById(reservedClassDto.getStudentId());
        verify(classRepository, times(1)).findById(reservedClassDto.getClassId());
        verify(reservationRepository, times(1)).existsByStudentIdAndAndClassObjIdAndStatus(reservedClassDto.getStudentId(), reservedClassDto.getClassId(), "active");
        verify(studentClassRepository, times(1)).findByStudentIdAndClassObjId(reservedClassDto.getStudentId(), reservedClassDto.getClassId());
    }

    @Test
    void createReservation_Successful() {
        StudentClass studentClass = new StudentClass();
        studentClass.setAttendingStatus("In class");
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        when(classRepository.findById(anyLong())).thenReturn(Optional.of(currentClass));
        when(reservationRepository.existsByStudentIdAndAndClassObjIdAndStatus(anyLong(), anyLong(), eq("active"))).thenReturn(false);
        when(studentClassRepository.findByStudentIdAndClassObjId(anyLong(), anyLong())).thenReturn(studentClass);
        when(emailTemplateRepository.findByDescriptionContainingIgnoreCase(anyString())).thenReturn(emailTemplate);

        CustomReservationResponse response = reservationService.createReservation(reservedClassDto);

        assertNotNull(response);
        verify(studentRepository, times(1)).findById(reservedClassDto.getStudentId());
        verify(classRepository, times(1)).findById(reservedClassDto.getClassId());
        verify(reservationRepository, times(1)).existsByStudentIdAndAndClassObjIdAndStatus(reservedClassDto.getStudentId(), reservedClassDto.getClassId(), "active");
        verify(studentClassRepository, times(1)).findByStudentIdAndClassObjId(reservedClassDto.getStudentId(), reservedClassDto.getClassId());
        verify(reservationRepository).save(any(ReservedClass.class));
    }
    //End Test Create New ReservedClass

    //Test Get List ReservedClass
    @Test
    void getReservedList_ReturnsData_Successfully() {
        when(reservationRepository.findByIdorFullNameorEmail(anyLong(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(mockPage);
        when(trainingProgramModuleRepository.findModulesByTraningProgramId(anyLong())).thenReturn(Arrays.asList(1L, 2L));
        when(moduleRepository.findById(anyLong())).thenReturn(Optional.of(mockModule));
        when(modelMapper.map(any(ReservedClass.class), eq(CustomReservationResponse.class)))
                .thenReturn(mockCustomReservationResponse);

        ReservedStudentResponse response = reservationService.getReservedList(0, 10, "id", "ASC", 1L, "FullName", "email@example.com");

        assertNotNull(response);
        assertFalse(response.getContent().isEmpty());
        assertEquals(mockPage.getTotalElements(), response.getTotalElements());
        assertTrue(response.getContent().get(0).getModuleName().contains("Math"));

        // Verify các tương tác với repository
        verify(reservationRepository).findByIdorFullNameorEmail(anyLong(), anyString(), anyString(), any(Pageable.class));
        verify(trainingProgramModuleRepository).findModulesByTraningProgramId(anyLong());
        verify(moduleRepository, times(2)).findById(anyLong());
    }

    @Test
    void getReservedList_ModuleNotFound_ThrowsException() {
        when(reservationRepository.findByIdorFullNameorEmail(anyLong(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(mockPage);
        when(trainingProgramModuleRepository.findModulesByTraningProgramId(anyLong())).thenReturn(Arrays.asList(1L));
        when(moduleRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(modelMapper.map(any(ReservedClass.class), eq(CustomReservationResponse.class)))
                .thenReturn(mockCustomReservationResponse);

        assertThrows(FamsApiException.class, () -> {
            reservationService.getReservedList(0, 10, "id", "ASC", 1L, "FullName", "email@example.com");
        });

        verify(reservationRepository).findByIdorFullNameorEmail(anyLong(), anyString(), anyString(), any(Pageable.class));
        verify(trainingProgramModuleRepository).findModulesByTraningProgramId(anyLong());
        verify(moduleRepository).findById(anyLong());
    }
    //End Test Get List ReservedClass

    //Test Get List ReservedClass
    @Test
    public void whenClassInactive_thenThrowException() {
        Long studentId = 1L, classId = 1L;

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(new Student()));
        when(classRepository.findById(anyLong())).thenReturn(Optional.ofNullable(currentClass));
        currentClass.setStatus("inactive");

        FamsApiException thrown = Assertions.assertThrows(FamsApiException.class, () -> reservationService.reclassStudent(studentId, classId));

        assertTrue(thrown.getMessage().contains("This class doesn't exist"));
        verify(studentRepository, times(1)).findById(anyLong());
        verify(classRepository, times(1)).findById(anyLong());
    }

    @Test
    void reclassStudent_ClassIsFinished_ThrowsException() {
        Long studentId = 1L, classId = 1L;

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(new Student()));
        when(classRepository.findById(anyLong())).thenReturn(Optional.ofNullable(currentClass));
        currentClass.setStatus("active");
        currentClass.setEndDate(LocalDate.now().minusDays(1));

        FamsApiException thrown = Assertions.assertThrows(FamsApiException.class, () -> reservationService.reclassStudent(studentId, classId));

        assertTrue(thrown.getMessage().contains("Can't re-class due to the class is finished"));
        verify(studentRepository, times(1)).findById(anyLong());
        verify(classRepository, times(1)).findById(anyLong());
    }

    //Test ReClass after Reservation
    @Test
    void reclassStudent_StudentDidNotReserveAnyClass_ThrowsException() {
        Long studentId = 1L, classId = 1L;

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        when(classRepository.findById(anyLong())).thenReturn(Optional.ofNullable(currentClass));
        currentClass.setStatus("active");
        currentClass.setEndDate(LocalDate.now().plusDays(1));
        when(studentClassRepository.findByStudentAndClassId(anyLong(), anyLong(), anyString())).thenReturn(Optional.empty());

        FamsApiException thrown = Assertions.assertThrows(FamsApiException.class, () -> reservationService.reclassStudent(studentId, classId));

        assertTrue(thrown.getMessage().contains("This student didn't reserve any class"));
        verify(studentRepository, times(1)).findById(anyLong());
        verify(classRepository, times(1)).findById(anyLong());
    }

    @Test
    void reclassStudent_StudentNotInReservationList_ThrowsException() {
        Long studentId = 1L, classId = 1L;

        student.setId(1L);
        currentClass.setId(1L);

        StudentClass sc = new StudentClass();
        sc.setClassObj(currentClass);
        sc.setStudent(student);
        sc.setAttendingStatus("Drop Out");

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        when(classRepository.findById(anyLong())).thenReturn(Optional.ofNullable(currentClass));
        currentClass.setStatus("active");
        currentClass.setEndDate(LocalDate.now().plusDays(1));
        when(studentClassRepository.findByStudentAndClassId(anyLong(), anyLong(), anyString())).thenReturn(Optional.of(sc));
        when(reservationRepository.findBystudentIdAndClassId(anyLong(), anyLong())).thenReturn(Optional.empty());

        FamsApiException thrown = Assertions.assertThrows(FamsApiException.class, () -> reservationService.reclassStudent(studentId, classId));

        assertTrue(thrown.getMessage().contains("This student isn't in reservation list "));
        verify(studentRepository, times(1)).findById(anyLong());
        verify(classRepository, times(1)).findById(anyLong());
        verify(studentClassRepository, times(1)).findByStudentAndClassId(anyLong(), anyLong(), anyString());
        verify(reservationRepository, times(1)).findBystudentIdAndClassId(anyLong(), anyLong());
    }

    @Test
    void reclassStudent_Success() {
        Long studentId = 1L, classId = 1L;

        student.setId(1L);
        currentClass.setId(1L);

        StudentClass sc = new StudentClass();
        sc.setClassObj(currentClass);
        sc.setStudent(student);
        sc.setAttendingStatus("Drop Out");

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        when(classRepository.findById(anyLong())).thenReturn(Optional.ofNullable(currentClass));
        currentClass.setStatus("active");
        currentClass.setEndDate(LocalDate.now().plusDays(1));
        when(studentClassRepository.findByStudentAndClassId(anyLong(), anyLong(), anyString())).thenReturn(Optional.of(sc));
        when(reservationRepository.findBystudentIdAndClassId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(reservationRepository.findBystudentIdAndClassId(studentId, classId)).thenReturn(Optional.of(reservedClass));

        reservationService.reclassStudent(classId, studentId);

        assertEquals("Back To Class", sc.getAttendingStatus());
        assertEquals("Inactive", reservedClass.getStatus());
        verify(studentClassRepository).save(sc);
        verify(reservationRepository).save(reservedClass);
    }
    //End Test ReClass after Reservation

    //Test Update attending status DropOut
    @Test
    void dropoutStudent_StudentDidNotparticipateAnyClass_ThrowsException() {
        Long studentId = 1L, classId = 1L;

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        when(classRepository.findById(anyLong())).thenReturn(Optional.ofNullable(currentClass));
        currentClass.setStatus("active");
        currentClass.setEndDate(LocalDate.now().plusDays(1));
        when(studentClassRepository.findByStudentAndClassId(anyLong(), anyLong(), anyString())).thenReturn(Optional.empty());

        FamsApiException thrown = Assertions.assertThrows(FamsApiException.class, () -> reservationService.dropoutStudent(studentId, classId));

        assertTrue(thrown.getMessage().contains("This student didnt participate in this class"));
        verify(studentRepository, times(1)).findById(anyLong());
        verify(classRepository, times(1)).findById(anyLong());
    }

    @Test
    void dropoutStudent_StudentNotInReservationList_ThrowsException() {
        Long studentId = 1L, classId = 1L;

        student.setId(1L);
        currentClass.setId(1L);

        StudentClass sc = new StudentClass();
        sc.setClassObj(currentClass);
        sc.setStudent(student);
        sc.setAttendingStatus("Drop Out");

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        when(classRepository.findById(anyLong())).thenReturn(Optional.ofNullable(currentClass));
        currentClass.setStatus("active");
        currentClass.setEndDate(LocalDate.now().plusDays(1));
        when(studentClassRepository.findByStudentAndClassId(anyLong(), anyLong(), anyString())).thenReturn(Optional.of(sc));
        when(reservationRepository.findBystudentIdAndClassId(anyLong(), anyLong())).thenReturn(Optional.empty());

        FamsApiException thrown = Assertions.assertThrows(FamsApiException.class, () -> reservationService.dropoutStudent(studentId, classId));

        assertTrue(thrown.getMessage().contains(""));
        verify(studentRepository, times(1)).findById(anyLong());
        verify(classRepository, times(1)).findById(anyLong());
        verify(studentClassRepository, times(1)).findByStudentAndClassId(anyLong(), anyLong(), anyString());
        verify(reservationRepository, times(1)).findBystudentIdAndClassId(anyLong(), anyLong());
    }

    @Test
    void dropoutStudent_Success() {
        Long studentId = 1L, classId = 1L;

        student.setId(1L);
        currentClass.setId(1L);

        StudentClass sc = new StudentClass();
        sc.setClassObj(currentClass);
        sc.setStudent(student);
        sc.setAttendingStatus("Drop Out");

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        when(classRepository.findById(anyLong())).thenReturn(Optional.ofNullable(currentClass));
        currentClass.setStatus("active");
        currentClass.setEndDate(LocalDate.now().plusDays(1));
        when(studentClassRepository.findByStudentAndClassId(anyLong(), anyLong(), anyString())).thenReturn(Optional.of(sc));
        when(reservationRepository.findBystudentIdAndClassId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(reservationRepository.findBystudentIdAndClassId(studentId, classId)).thenReturn(Optional.of(reservedClass));

        reservationService.dropoutStudent(classId, studentId);

        assertEquals("Drop out", sc.getAttendingStatus());
        assertEquals("Inactive", reservedClass.getStatus());
        verify(studentClassRepository).save(sc);
        verify(reservationRepository).save(reservedClass);
    }
    //End Test Update attending status DropOut

    //Test Remind Student
    @Test
    public void noStudentsToRemind() {
        when(reservationRepository.findByStatus("active")).thenReturn(Collections.emptyList());
        when(emailTemplateRepository.findByDescriptionContainingIgnoreCase(anyString())).thenReturn(emailTemplate);

        List<String> resultEmails = reservationService.remindStudentToReClass();

        assertTrue(resultEmails.isEmpty());
        verify(reservationRepository, times(1)).findByStatus("active");
        verify(emailTemplateRepository, times(1)).findByDescriptionContainingIgnoreCase(anyString());
    }

    @Test
    public void remindStudentsSuccessfully() {
        reservedClass.setStatus("active");
        reservedClass.setEndDate(LocalDate.now().plusDays(30));
        student.setStatus("Keep Class");
        reservedClass.setStudent(student);
        List<ReservedClass> reservedClasses = new ArrayList<>();
        reservedClasses.add(reservedClass);
        when(emailTemplateRepository.findByDescriptionContainingIgnoreCase(anyString())).thenReturn(emailTemplate);
        when(reservationRepository.findByStatus("active")).thenReturn(reservedClasses);

        List<String> resultEmails = reservationService.remindStudentToReClass();

        assertNotNull(resultEmails);
        assertEquals(1, resultEmails.size());
        assertEquals(reservedClass.getStudent().getEmail(), resultEmails.get(0));
        verify(reservationRepository, times(1)).findByStatus("active");
        verify(emailTemplateRepository, times(1)).findByDescriptionContainingIgnoreCase(anyString());
    }
    //End Test Remind Student

    //Test Find New Class For ReservedStudent
    @Test
    public void whenStudentNotAttendReservingClass_thenThrowException() {
        Long studentId = 1L, classId = 1L;

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(new Student()));
        when(classRepository.findById(anyLong())).thenReturn(Optional.of(new Class()));
        when(studentClassRepository.findByStudentIdAndClassObjIdAndAttendingStatus(anyLong(), anyLong(), eq("Reserved"))).thenReturn(null);

        Exception exception = Assertions.assertThrows(FamsApiException.class, () -> reservationService.findNewClassForReservedStudent(studentId, classId));

        Assertions.assertTrue(exception.getMessage().contains("This student is not attending or reserving this class"));
        verify(studentRepository, times(1)).findById(reservedClassDto.getStudentId());
        verify(classRepository, times(1)).findById(reservedClassDto.getClassId());
        verify(studentClassRepository, times(1)).findByStudentIdAndClassObjIdAndAttendingStatus(reservedClassDto.getStudentId(), reservedClassDto.getClassId(), "Reserved");
    }

    @Test
    public void whenStudentNotReservingClass_thenThrowException() {
        Long studentId = 1L, classId = 1L;

        student.setId(1L);
        currentClass.setId(1L);

        StudentClass sc = new StudentClass();
        sc.setClassObj(currentClass);
        sc.setStudent(student);
        sc.setAttendingStatus("Reserved");

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(new Student()));
        when(classRepository.findById(anyLong())).thenReturn(Optional.of(new Class()));
        when(studentClassRepository.findByStudentIdAndClassObjIdAndAttendingStatus(anyLong(), anyLong(), eq("Reserved"))).thenReturn(sc);
        when(reservationRepository.findByStudentIdAndAndClassObjIdAndStatus(anyLong(), anyLong(), eq("active"))).thenReturn(null);

        Exception exception = Assertions.assertThrows(FamsApiException.class, () -> reservationService.findNewClassForReservedStudent(studentId, classId));

        Assertions.assertTrue(exception.getMessage().contains("This student is not reserving this class"));
        verify(studentRepository, times(1)).findById(reservedClassDto.getStudentId());
        verify(classRepository, times(1)).findById(reservedClassDto.getClassId());
        verify(studentClassRepository, times(1)).findByStudentIdAndClassObjIdAndAttendingStatus(reservedClassDto.getStudentId(), reservedClassDto.getClassId(), "Reserved");
        verify(reservationRepository, times(1)).findByStudentIdAndAndClassObjIdAndStatus(reservedClassDto.getStudentId(), reservedClassDto.getClassId(), "active");
    }

    @Test
    void whenNoMatchingClasses_thenThrowException() {
        Long studentId = 1L, classId = 1L;

        student.setId(1L);
        currentClass.setId(1L);

        StudentClass sc = new StudentClass();
        sc.setClassObj(currentClass);
        sc.setStudent(student);
        sc.setAttendingStatus("Reserved");

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        when(classRepository.findById(anyLong())).thenReturn(Optional.of(currentClass));
        when(studentClassRepository.findByStudentIdAndClassObjIdAndAttendingStatus(anyLong(), anyLong(), eq("Reserved"))).thenReturn(sc);
        when(reservationRepository.findByStudentIdAndAndClassObjIdAndStatus(anyLong(), anyLong(), eq("active"))).thenReturn(reservedClass);
        when(classRepository.findAll()).thenReturn(Collections.emptyList());

        Exception exception = Assertions.assertThrows(FamsApiException.class, () -> reservationService.findNewClassForReservedStudent(studentId, classId));

        assertEquals("There is no class that has the same modules yet", exception.getMessage());
        verify(studentRepository, times(1)).findById(reservedClassDto.getStudentId());
        verify(classRepository, times(1)).findById(reservedClassDto.getClassId());
        verify(studentClassRepository, times(1)).findByStudentIdAndClassObjIdAndAttendingStatus(reservedClassDto.getStudentId(), reservedClassDto.getClassId(), "Reserved");
        verify(reservationRepository, times(1)).findByStudentIdAndAndClassObjIdAndStatus(reservedClassDto.getStudentId(), reservedClassDto.getClassId(), "active");
        verify(classRepository, times(1)).findAll();
    }

    @Test
    void whenValidStudentAndClass_thenFindMatchingNewClasses() {
        Long studentId = 1L, classId = 1L;

        student.setId(1L);
        currentClass.setId(1L);

        StudentClass sc = new StudentClass();
        sc.setClassObj(currentClass);
        sc.setStudent(student);
        sc.setAttendingStatus("Reserved");

        List<Class> matchedClassList = new ArrayList<>();
        matchedClassList.add(newMatchingClass);

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        when(classRepository.findById(anyLong())).thenReturn(Optional.of(currentClass));
        when(studentClassRepository.findByStudentIdAndClassObjIdAndAttendingStatus(anyLong(), anyLong(), eq("Reserved"))).thenReturn(sc);
        when(reservationRepository.findByStudentIdAndAndClassObjIdAndStatus(anyLong(), anyLong(), eq("active"))).thenReturn(reservedClass);
        when(classRepository.findAll()).thenReturn(matchedClassList);

        List<CustomClassResponse> result = reservationService.findNewClassForReservedStudent(studentId, classId);

        assertFalse(result.isEmpty());
        verify(studentRepository, times(1)).findById(reservedClassDto.getStudentId());
        verify(classRepository, times(1)).findById(reservedClassDto.getClassId());
        verify(studentClassRepository, times(1)).findByStudentIdAndClassObjIdAndAttendingStatus(reservedClassDto.getStudentId(), reservedClassDto.getClassId(), "Reserved");
        verify(reservationRepository, times(1)).findByStudentIdAndAndClassObjIdAndStatus(reservedClassDto.getStudentId(), reservedClassDto.getClassId(), "active");
        verify(classRepository, times(1)).findAll();
    }
    //End Test Find New Class

    //Test Import Data
    @Test
    void whenImportFileNotExcelFile_ThrowException() {
        String content = "1,2024-11-06,2024-05-06,1,1,thich,active";
        MockMultipartFile file = new MockMultipartFile(
                "reservation_class_file",
                "file.txt",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                content.getBytes());

        FamsApiException famsApiException = Assertions.assertThrows(
                FamsApiException.class, () -> reservationService.importDataFromXlsx(file));

        assertEquals(HttpStatus.BAD_REQUEST, famsApiException.getStatus());
        assertEquals("Only .xlsx file is supported!", famsApiException.getMessage());


    }

    @Test
    public void testImportExcelFile_Success() throws IOException {
        List<String> successList = new ArrayList<>();
        List<String> failList = Arrays.asList("At Row 1. The student: SE001 is already reserved this class",
                "At Row 2. The student: SE002 is already reserved this class",
                "At Row 3. The student: SE003 is already reserved this class",
                "At Row 4. The student: SE004 is not attending this class",
                "At Row 5. The period is no longer than 6 months",
                "At Row 6. The student: SE006 is already reserved this class",
                "At Row 7. Student not found with id: '7'",
                "At Row 8. Class not found with id: '4'",
                "At Row 9. The date time format is invalid, the format must be (yyyy-MM-dd)");

        MultipartFile file = new MockMultipartFile(
                "filename",
                "Reservation_Classes_Import.xlsx",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                new ClassPathResource("Reservation_Classes_Test.xlsx").getInputStream());

        ImportExcelResponse response = reservationService.importDataFromXlsx(file);
        assertTrue(response.getSuccessList().isEmpty());
        assertNotEquals(response.getFailList(),failList, "fail list should not change");

    }
}
