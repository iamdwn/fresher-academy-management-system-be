package java03.team01.FAMS;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import java03.team01.FAMS.model.entity.*;
import java03.team01.FAMS.model.exception.FamsApiException;
import java03.team01.FAMS.model.payload.dto.*;
import java03.team01.FAMS.model.payload.responseModel.CustomReservationEmailResponse;
import java03.team01.FAMS.model.payload.responseModel.EmailTemplatesResponse;
import java03.team01.FAMS.repository.EmailSendRepository;
import java03.team01.FAMS.repository.EmailTemplateRepository;
import java03.team01.FAMS.repository.StudentRepository;
import java03.team01.FAMS.repository.UserRepository;
import java03.team01.FAMS.service.EmailService;
import java03.team01.FAMS.service.impl.EmailServiceImp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {
    @Mock
    EmailService emailService;

    @Mock
    EmailTemplateRepository emailTemplateRepository;

    @Mock
    EmailSendRepository emailSendRepository;

    @Spy
    ModelMapper modelMapper;
    @Mock
    UserRepository userRepository;
    @Mock
    JavaMailSender mailSender;

    @InjectMocks
    EmailServiceImp emailServiceImp;
    @Mock
    StudentRepository studentRepository;


    @Test
    void testGetAllEmailTemplate() {
        // Mocking repository response
        List<EmailTemplate> emailTemplatesList = new ArrayList<>();
        EmailTemplate emailTemplate1 = new EmailTemplate(); // Create email template objects as needed
        EmailTemplate emailTemplate2 = new EmailTemplate();
        emailTemplatesList.add(emailTemplate1);
        emailTemplatesList.add(emailTemplate2);
        Page<EmailTemplate> page = new PageImpl<>(emailTemplatesList);

        when(emailTemplateRepository.findAll(any(Pageable.class))).thenReturn(page);

        // Call the service method
        EmailTemplatesResponse response = emailServiceImp.getAllEmailTemplate(0, 10, "id", "asc");

        // Assertions
        assertEquals(2, response.getContent().size());

    }

    @Test
    void testGetAllEmailTemplateByCategory() {
        // Mocking repository response
        List<EmailTemplate> emailTemplatesList = new ArrayList<>();
        EmailTemplate emailTemplate1 = new EmailTemplate(); // Create email template objects as needed
        EmailTemplate emailTemplate2 = new EmailTemplate();

        emailTemplatesList.add(emailTemplate1);
        emailTemplatesList.add(emailTemplate2);
        Page<EmailTemplate> page = new PageImpl<>(emailTemplatesList);

        when(emailTemplateRepository.findByCategory(any(String.class),any(Pageable.class))).thenReturn(page);

        // Call the service method
        EmailTemplatesResponse response = emailServiceImp.getAllEmailTemplateByCategory("Inform",0, 10, "id", "asc");

        // Assertions
        assertEquals(2, response.getContent().size());

    }
    @Test
    void testGetAllEmailTemplateByCategory_ALL() {
        // Mocking repository response
        List<EmailTemplate> emailTemplatesList = new ArrayList<>();
        EmailTemplate emailTemplate1 = new EmailTemplate(); // Create email template objects as needed
        EmailTemplate emailTemplate2 = new EmailTemplate();

        emailTemplatesList.add(emailTemplate1);
        emailTemplatesList.add(emailTemplate2);
        Page<EmailTemplate> page = new PageImpl<>(emailTemplatesList);
        when(emailTemplateRepository.findAll(any(Pageable.class))).thenReturn(page);


        // Call the service method
        EmailTemplatesResponse response = emailServiceImp.getAllEmailTemplateByCategory("Informhhh",0, 10, "id", "asc");

        // Assertions
        assertEquals(2, response.getContent().size());

    }
    @Test
    void testGetReservationEmailSend() {
        // Mocking repository response
        List<EmailSend> emailSendsList = new ArrayList<>();
        User sender = user();
        EmailTemplate template = template2();
        EmailSend emailSend =new EmailSend();
        emailSend.setSender(sender);
        emailSend.setEmailTemplate(template);
        emailSend.setReceiverType("STUDENT");
        emailSend.setContent("test cOntent");
        Set<EmailSendStudent> emailSendStudentDto1 = new HashSet<>();
        emailSendStudentDto1.add(new EmailSendStudent(1L,emailSend,stu1()));
        emailSendStudentDto1.add(new EmailSendStudent(2L,emailSend,stu2()));
        emailSend.setEmailSendStudents(emailSendStudentDto1);
        // Create more EmailSend objects as needed for testing
        emailSendsList.add(emailSend);
        Page<EmailSend> page = new PageImpl<>(emailSendsList);

        when(emailSendRepository.findAll(any(Pageable.class))).thenReturn(page);

        // Call the service method
        CustomReservationEmailResponse response = emailServiceImp.getReservationEmailSend(0, 10, "id", "asc");

        // Assertions
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        // Add more assertions based on the expected behavior of the method
    }
    @Test
    public void createEmailTemplate(){
        EmailTemplateDto emailTemplateDto = EmailTemplateDto.builder()
                .type("STUDENT")
                .name("Example Template")
                .description("This is an example email template.")
                .createdDate(LocalDate.now())
                .createdBy("John Doe")
                .category("Inform")
                .status(true)
                .build();
        EmailTemplate  emailTemplate = EmailTemplate.builder()
                .type("STUDENT")
                .name("Example Template")
                .description("[Content].")
                .createdDate(LocalDate.now())
                .createdBy("John Doe")
                .category("Inform")
                .status(true)
                .build();
//        EmailTemplate emailTemplate = modelMapper.map(emailTemplateDto, EmailTemplate.class;
//        when(modelMapper.map(emailTemplateDto, EmailTemplate.class)).then(emailTemplate = modelMapper.map(emailTemplateDto, EmailTemplate.class));
        when(emailTemplateRepository.save(Mockito.any(EmailTemplate.class))).thenReturn(emailTemplate);

        EmailTemplateDto emailTemplateDto1= emailServiceImp.createEmailTemplate(emailTemplateDto);
        System.out.println(emailTemplateDto1.getName());
        assertNotNull(emailTemplateDto1);
    }



    private User user(){
        return  User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .dob(LocalDate.of(1990, 5, 15))
                .address("123 Main St, City")
                .gender("Male")
                .phone("123-456-7890")
                .username("johndoe")
                .password("password123").role(role())
                .build();
    }
    private User user1(){
        return  User.builder()
                .id(2L)
                .fullName("John Doe")
                .email("john2@example.com")
                .dob(LocalDate.of(1990, 5, 15))
                .address("123 Main St, City")
                .gender("Male")
                .phone("123-456-7890")
                .username("johndoe2")
                .password("password123").role(role())
                .build();
    }
    private User user2(){
        return  User.builder()
                .id(3L)
                .fullName("John Doe")
                .email("john3@example.com")
                .dob(LocalDate.of(1990, 5, 15))
                .address("123 Main St, City")
                .gender("Male")
                .phone("123-456-7890")
                .username("johndoe3")
                .password("password123").role(role())
                .build();
    }
    private Role role(){
        return  new Role(2, "ADMIN",new HashSet<>());
    }

    private Set<User> listUser(){
        Set<User> list =new HashSet<>();
        list.add(user1());
        list.add(user2());
        return list;
    }

    private Student stu1 (){
        return  Student.builder()
                .id(1L)
                .studentCode("S12345")
                .fullName("John Doe")
                .dob(LocalDate.of(1995, 10, 20))
                .gender("Male")
                .phone("123-456-7890")
                .email("john@example.com")
                .school("Example University")
                .major("Computer Science")
                .graduatedDate(LocalDate.of(2020, 5, 15))
                .gpa(3.5f)
                .address("123 Main St, City")
                .faAccount("john_doe")
                .type("Regular")
                .status("Active")
                .reCer("123456")
                .joinedDate(LocalDate.of(2020, 1, 1))
                .area("Urban")
                .build();
    }
    private Student stu2 (){
        return  Student.builder()
                .id(2L)
                .studentCode("S12345")
                .fullName("John Wickkkkk")
                .dob(LocalDate.of(1995, 10, 20))
                .gender("Male")
                .phone("123-456-7890")
                .email("johnynodog@example.com")
                .school("Example University")
                .major("Computer Science")
                .graduatedDate(LocalDate.of(2020, 5, 15))
                .gpa(3.5f)
                .address("123 Main St, City")
                .faAccount("john_doe1")
                .type("Regular")
                .status("Active")
                .reCer("123456")
                .joinedDate(LocalDate.of(2020, 1, 1))
                .area("Urban")
                .build();
    }
    private  List<Student> listStu(){
        List<Student>  list =new ArrayList<>();
        list.add(stu1());
        list.add(stu2());
        return list;
    }
    private EmailTemplate template1(){
        return  EmailTemplate.builder()
                .id(1L)
                .type("USER")
                .name("Example Template")
                .description("This is an example email template [Content].")
                .createdDate(LocalDate.now())
                .createdBy("John Doe")
                .category("Inform")
                .status(true)
                .build();
    }

    private EmailTemplate template2(){
        return  EmailTemplate.builder()
                .id(1L)
                .type("STUDENT")
                .name("Example Template")
                .description("This is an example email template [Content].")
                .createdDate(LocalDate.now())
                .createdBy("John Doe")
                .category("Inform")
                .status(true)
                .build();
    }


    @Test
    public void testSendEmail_SuccessUserReceiver() throws Exception {
        // Mock data


        User sender = user();
        EmailTemplate template = template1();
        EmailSend emailSend =new EmailSend();
        emailSend.setSender(sender);
        emailSend.setEmailTemplate(template);
        emailSend.setReceiverType("USER");
        emailSend.setContent("test cOntent");
        Set<EmailSendUser> emailSendStudentDto1 = new HashSet<>();
        emailSendStudentDto1.add(new EmailSendUser(1L,emailSend,user1()));
        emailSendStudentDto1.add(new EmailSendUser(2L,emailSend,user2()));
        emailSend.setEmailSendUsers(emailSendStudentDto1);

        // Mocked behaviors
        when(userRepository.findById(user().getId())).thenReturn(Optional.of(sender));
        when(emailTemplateRepository.findById(template.getId())).thenReturn(Optional.of(template));
        when(emailSendRepository.save(Mockito.any(EmailSend.class))).thenReturn(emailSend);

        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);

        // Create EmailSendDto
        EmailSendDto emailSendDto = new EmailSendDto();
        emailSendDto.setTemplateId(template.getId());
        emailSendDto.setSenderId(sender.getId());
        emailSendDto.setReceiverType("USER");
        emailSendDto.setContent("test cOntre");
        EmailSendUserDto emailSendStudentDto = new EmailSendUserDto();
        emailSendStudentDto.setUserId(user1().getId());
        emailSendStudentDto.setUserId(user2().getId());

        emailSendDto.setEmailSendUsers(Set.of(emailSendStudentDto));
        // Call the method
        String result = emailServiceImp.sendEmail(emailSendDto);

        // Assertions
        assertEquals("Mail Sent Successfully...", result);

    }

    @Test
    public void testSendEmail_SuccessStudentReceiver() throws Exception {
        // Mock data


        User sender = user();
        EmailTemplate template = template2();
        EmailSend emailSend =new EmailSend();
        emailSend.setSender(sender);
        emailSend.setEmailTemplate(template);
        emailSend.setReceiverType("STUDENT");
        emailSend.setContent("test cOntent");
        Set<EmailSendStudent> emailSendStudentDto1 = new HashSet<>();
        emailSendStudentDto1.add(new EmailSendStudent(1L,emailSend,stu1()));
        emailSendStudentDto1.add(new EmailSendStudent(2L,emailSend,stu2()));
        emailSend.setEmailSendStudents(emailSendStudentDto1);

        // Mocked behaviors



        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);

        // Create EmailSendDto
        EmailSendDto emailSendDto = new EmailSendDto();
        emailSendDto.setTemplateId(template.getId());
        emailSendDto.setSenderId(sender.getId());
        emailSendDto.setReceiverType("STUDENT");
        emailSendDto.setContent("test cOntre");
        EmailSendStudentDto emailSendStudentDto = new EmailSendStudentDto();
        emailSendStudentDto.setStudentId(stu1().getId());
        emailSendStudentDto.setStudentId(stu2().getId());
        emailSendDto.setEmailSendStudents(Set.of(emailSendStudentDto));


        when(userRepository.findById(user().getId())).thenReturn(Optional.of(sender));
        when(emailTemplateRepository.findById(template.getId())).thenReturn(Optional.of(template));
        when(emailSendRepository.save(Mockito.any(EmailSend.class))).thenReturn(emailSend);
        when(studentRepository.findById(any())).thenReturn(Optional.of(stu1()));

        // Call the method
        String result = emailServiceImp.sendEmail(emailSendDto);

        // Assertions
        assertEquals("Mail Sent Successfully...", result);

    }
    @Test
    public void testSendEmail_SuccessBothReceiverType() throws Exception {
        // Mock data


        User sender = user();
        EmailTemplate template = template2();
        EmailSend emailSend =new EmailSend();
        emailSend.setSender(sender);
        emailSend.setEmailTemplate(template);
        emailSend.setReceiverType("Both");
        emailSend.setContent("test cOntent");
        Set<EmailSendStudent> emailSendStudentDto0 = new HashSet<>();
        emailSendStudentDto0.add(new EmailSendStudent(1L,emailSend,stu1()));
        emailSendStudentDto0.add(new EmailSendStudent(2L,emailSend,stu2()));
        emailSend.setEmailSendStudents(emailSendStudentDto0);
        Set<EmailSendUser> emailSendStudentDto1 = new HashSet<>();
        emailSendStudentDto1.add(new EmailSendUser(1L,emailSend,user1()));
        emailSendStudentDto1.add(new EmailSendUser(2L,emailSend,user2()));
        emailSend.setEmailSendUsers(emailSendStudentDto1);

        // Mocked behaviors



        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);

        // Create EmailSendDto
        EmailSendDto emailSendDto = new EmailSendDto();
        emailSendDto.setTemplateId(template.getId());
        emailSendDto.setSenderId(sender.getId());
        emailSendDto.setReceiverType("Both");
        emailSendDto.setContent("test cOntre");
        EmailSendStudentDto emailSendStudentDto = new EmailSendStudentDto();
        emailSendStudentDto.setStudentId(stu1().getId());
        emailSendStudentDto.setStudentId(stu2().getId());
        emailSendDto.setEmailSendStudents(Set.of(emailSendStudentDto));
        EmailSendUserDto emailSendStudentDto2 = new EmailSendUserDto();
        emailSendStudentDto2.setUserId(user1().getId());
        emailSendStudentDto2.setUserId(user2().getId());
        emailSendDto.setEmailSendUsers(Set.of(emailSendStudentDto2));



        when(userRepository.findById(user().getId())).thenReturn(Optional.of(sender));
        when(emailTemplateRepository.findById(template.getId())).thenReturn(Optional.of(template));
        when(emailSendRepository.save(Mockito.any(EmailSend.class))).thenReturn(emailSend);
        when(studentRepository.findById(any())).thenReturn(Optional.of(stu1()));

        // Call the method
        String result = emailServiceImp.sendEmail(emailSendDto);

        // Assertions
        assertEquals("Mail Sent Successfully...", result);

    }
    @Test
    public  void  testUpdateEmailTemplate_SuccessUpdate(){
        Long id = 1L;
        EmailTemplate template1 = template1();
        EmailTemplateUpdateDto emailTemplateUpdateDto = EmailTemplateUpdateDto.builder()
                .type("Both")
                .name("Example Template")
                .description("This is an  email template [Content].")
                .createdDate(LocalDate.now())
                .createdBy("John Wick")
                .category("Remind")
                .status(true)
                .build();
        when(emailTemplateRepository.findById(id)).thenReturn(Optional.ofNullable(template1));
        when(emailTemplateRepository.save(template1)).thenReturn(template1);

        EmailTemplateDto response =emailServiceImp.updateEmailTemplate(id,emailTemplateUpdateDto);

        assertNotNull(response);

    }
    @Test
    public  void  testUpdateEmailTemplate_NotFoundId(){
        Long id = 2L;

        EmailTemplateUpdateDto emailTemplateUpdateDto = EmailTemplateUpdateDto.builder()
                .type("Both")
                .name("Example Template")
                .description("This is an  email template [Content].")
                .createdDate(LocalDate.now())
                .createdBy("John Wick")
                .category("Remind")
                .status(true)
                .build();
//        when(emailTemplateRepository.findById(id)).thenReturn(Optional.ofNullable(template1));
//        when(emailTemplateRepository.save(template1)).thenReturn(template1);
        when(emailTemplateRepository.findById(id)).thenReturn(Optional.empty());

//        EmailTemplateDto response =emailServiceImp.updateEmailTemplate(id,emailTemplateUpdateDto);

        FamsApiException exception = assertThrows(FamsApiException.class, () -> {
            emailServiceImp.updateEmailTemplate(id, emailTemplateUpdateDto);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Email template not found with ID: " + id, exception.getMessage());

    }

    @Test
    public void  testUpdateEmailTemplateStatus_ChangeInactiveSuccess(){
        Long id = 2L;
        EmailTemplate template =template1();
        template.setStatus(true);

        when(emailTemplateRepository.findById(id)).thenReturn(Optional.ofNullable(template));
        when(emailTemplateRepository.save(template)).thenReturn(template);
        EmailTemplateDto response = emailServiceImp.updateEmailTemplateStatus(id,"inactive");
        assertNotNull(response);

    }
    @Test
    public void  testUpdateEmailTemplateStatus_ChangeActiveSuccess(){
        Long id = 2L;
        EmailTemplate template =template1();
        template.setStatus(true);

        when(emailTemplateRepository.findById(id)).thenReturn(Optional.ofNullable(template));
        when(emailTemplateRepository.save(template)).thenReturn(template);
        EmailTemplateDto response = emailServiceImp.updateEmailTemplateStatus(id,"active");
        assertNotNull(response);

    }
    @Test
    public void  testUpdateEmailTemplateStatus_InvalidStatus(){
        Long id = 2L;
        EmailTemplate template =template1();
        template.setStatus(true);

        when(emailTemplateRepository.findById(id)).thenReturn(Optional.ofNullable(template));
//        when(emailTemplateRepository.save(template)).thenReturn(template);
        FamsApiException exception = assertThrows(FamsApiException.class, () -> {
            emailServiceImp.updateEmailTemplateStatus(id, "aaaaa");
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Status type not valid.", exception.getMessage());
    }

    @Test
    public  void  testUpdateEmailTemplateStatus_NotFoundId(){
        Long id = 2L;

        when(emailTemplateRepository.findById(id)).thenReturn(Optional.empty());

        FamsApiException exception = assertThrows(FamsApiException.class, () -> {
            emailServiceImp.updateEmailTemplateStatus(id, "active");
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Email template not found with ID: " + id, exception.getMessage());

    }


    @Test
    public void  testUpdateEmailTemplateDetail_InvalidStatus(){
        Long id = 2L;
        EmailTemplate template =template1();
        template.setStatus(true);

        when(emailTemplateRepository.findById(id)).thenReturn(Optional.ofNullable(template));
//        when(emailTemplateRepository.save(template)).thenReturn(template);

        EmailTemplateDto response =    emailServiceImp.getEmailTemplate(id);

        assertNotNull(response);
        assertEquals("Example Template", response.getName());
    }
}
