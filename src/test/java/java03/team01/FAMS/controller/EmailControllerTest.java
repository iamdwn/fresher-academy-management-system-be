package java03.team01.FAMS.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java03.team01.FAMS.controller.EmailController;
import java03.team01.FAMS.model.entity.EmailTemplate;
import java03.team01.FAMS.model.entity.RefreshToken;
import java03.team01.FAMS.model.entity.Role;
import java03.team01.FAMS.model.entity.User;
import java03.team01.FAMS.model.payload.dto.EmailSendDto;
import java03.team01.FAMS.model.payload.dto.EmailSendUserDto;
import java03.team01.FAMS.model.payload.dto.EmailTemplateDto;
import java03.team01.FAMS.model.payload.dto.EmailTemplateUpdateDto;
import java03.team01.FAMS.model.payload.responseModel.CustomReservationEmailResponse;
import java03.team01.FAMS.model.payload.responseModel.EmailSendResponse;
import java03.team01.FAMS.model.payload.responseModel.EmailTemplatesResponse;
import java03.team01.FAMS.repository.AccessTokenRepository;
import java03.team01.FAMS.repository.RefreshTokenRepository;
import java03.team01.FAMS.security.JwtTokenProvider;
import java03.team01.FAMS.service.AuthService;
import java03.team01.FAMS.service.EmailService;
import org.apache.commons.io.output.AppendableOutputStream;
import org.aspectj.lang.annotation.Before;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@WebMvcTest(controllers = EmailController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class EmailControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EmailService emailService;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ObjectMapper objectMapper;

    private EmailTemplate emailTemplate;
    private EmailTemplateDto emailTemplateDto;
    @MockBean
    JwtTokenProvider jwtTokenProvider;
    @MockBean
    AccessTokenRepository accessTokenRepository;
    @MockBean
    RefreshTokenRepository refreshTokenRepository;



    @Test
    public void EmailController_CreateEmailTemplalte_ReturnCreated() throws Exception {
        EmailTemplateDto emailTemplateDto = EmailTemplateDto.builder()
                .type("STUDENT")
                .name("Example Template")
                .description("This is an example email template.")
                .createdDate(LocalDate.now())
                .createdBy("John Doe")
                .category("Inform")
                .status(true)
                .build();
//        ResponseEntity<?> responseEntity =EmailController.createEmailTemplate(emailTemplateDto);
        ResultActions respone = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/email/email-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content( objectMapper.writeValueAsString(emailTemplateDto)));
        respone.andExpect(MockMvcResultMatchers.status().isCreated());
        System.out.println("Response content: " + respone.andReturn().getResponse().getStatus());
        System.out.println("Response content: " + respone.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void EmailController_CreateEmailTemplalte_ReturnFailed() throws Exception {
        EmailTemplateDto emailTemplateDto = EmailTemplateDto.builder()
                .type("STUDE")
                .name("Example Template")
                .description("This is an example email template.")
                .createdDate(LocalDate.now())
                .createdBy("John Doe")
                .category("Inform")
                .status(true)
                .build();
//        ResponseEntity<?> responseEntity =EmailController.createEmailTemplate(emailTemplateDto);
        ResultActions respone = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/email/email-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailTemplateDto)));
        respone.andExpect(MockMvcResultMatchers.status().isBadRequest());
        System.out.println("Response content: " + respone.andReturn().getResponse().getStatus());
        System.out.println("Response content: " + respone.andReturn().getResponse().getContentAsString());
    }

    @Test
    public  void  EmailController_GetAllEmailTemplate_ReturnSuccsess() throws Exception{
        EmailTemplateDto emailTemplate1 =EmailTemplateDto.builder()
                .type("USER")
                .name("Example Template")
                .description("This is an example email template [Content].")
                .createdDate(LocalDate.now())
                .createdBy("John Doe")
                .category("Inform")
                .status(true)
                .build();
        EmailTemplatesResponse responsedto =EmailTemplatesResponse.builder().pageSize(10).pageNo(0).last(true).content(Arrays.asList(emailTemplate1)).build();
        Mockito.when(emailService.getAllEmailTemplate(0,10,"id","asc")).thenReturn(responsedto);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/email/email-templates")
                .contentType(MediaType.APPLICATION_JSON));
        response.andExpect(MockMvcResultMatchers.status().isOk());
        System.out.println("Response content: " + response.andReturn().getResponse().getStatus());

    }
    @Test
    public void EmailController_GetReservationEmailSendHistory_ReturnSucess() throws Exception{
        EmailSendResponse emailSendResponse1= EmailSendResponse.builder()
                .sendDate(LocalDate.now())
                .No(1)
                .senderName("AAA")
                .action("weak")
                .build();
        CustomReservationEmailResponse responsedto =CustomReservationEmailResponse.builder().pageSize(10).pageNo(0).last(true).content(Arrays.asList(emailSendResponse1)).build();
        Mockito.when(emailService.getReservationEmailSend(0,10,"id","asc")).thenReturn(responsedto);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/email/history")
                .contentType(MediaType.APPLICATION_JSON));
        response.andExpect(MockMvcResultMatchers.status().isOk());
        System.out.println("Response content: " + response.andReturn().getResponse().getStatus());

    }
    @Test
    public  void  EmailController_Update_ReturnSuccess() throws  Exception{
    Long id =1L;
    EmailTemplateDto emailTemplateDto = EmailTemplateDto.builder()
                .type("STUDENT")
                .name("Example Template")
                .description("This is an example email template.")
                .createdDate(LocalDate.now())
                .createdBy("John Doe")
                .category("Inform")
                .status(true)
                .build();
        EmailTemplateUpdateDto emailTemplateDto1 = EmailTemplateUpdateDto.builder()
                .type("STUDENT")
                .name("Example Templ")
                .description("This is an example email template.")
                .createdDate(LocalDate.now())
                .createdBy("John Doe")
                .category("Inform")
                .status(true)
                .build();
    Mockito.when(emailService.updateEmailTemplate(id, emailTemplateDto1)).thenReturn(emailTemplateDto);
    ResultActions resultActions= mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/email/email-templates/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(emailTemplateDto1)));

    resultActions.andExpect(MockMvcResultMatchers.status().isOk())
          ;

    }

    @Test
    public  void  EmailController_UpdateStatus_ReturnSuccess() throws  Exception{
        Long id =1L;
        EmailTemplateDto emailTemplateDto = EmailTemplateDto.builder()
                .type("STUDENT")
                .name("Example Template")
                .description("This is an example email template.")
                .createdDate(LocalDate.now())
                .createdBy("John Doe")
                .category("Inform")
                .status(false)
                .build();
        Mockito.when(emailService.updateEmailTemplateStatus(id, "inactive")).thenReturn(emailTemplateDto);
        ResultActions resultActions= mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/email/email-templates/1/inactive")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailTemplateDto)));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk());

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

    private EmailTemplate template1(){
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
        return  new Role(2, "ROLE_ADMIN",new HashSet<>());
    }



    @Test
    public void testSendMail() throws Exception {
        // Create a sample EmailSendDto
        EmailSendDto emailSendDto = new EmailSendDto();
        emailSendDto.setTemplateId(template1().getId());
        emailSendDto.setSenderId(user().getId());
        emailSendDto.setReceiverType("USER");
        emailSendDto.setContent("test cOntre");
        EmailSendUserDto emailSendStudentDto = new EmailSendUserDto();
        emailSendStudentDto.setUserId(user1().getId());
        emailSendStudentDto.setUserId(user2().getId());
        emailSendDto.setEmailSendUsers(Set.of(emailSendStudentDto));

        // Mock the emailService.sendEmail method
        Mockito.when(emailService.sendEmail(Mockito.any(EmailSendDto.class)))
                .thenReturn("Mail Sent Successfully...");

        // Perform the POST request
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/email/sendMail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(emailSendDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Mail Sent Successfully..."));
    }

    @Test
    public void EmailController_EmailTemplateDetail_ReturnSuccess() throws Exception {
        Long pokemonId = 1L;
        Mockito.when(emailService.getEmailTemplate(pokemonId)).thenReturn(objectMapper.convertValue(template1(), EmailTemplateDto.class));

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/email/email-templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(objectMapper.convertValue(template1(), EmailTemplateDto.class))));

        response.andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    public void testGetAllEmailTempByCategory() throws Exception {
        // Mocking the email service behavior
        EmailTemplatesResponse response = new EmailTemplatesResponse(); // Create response as needed
        Mockito.when(emailService.getAllEmailTemplateByCategory(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(response);

        // Performing GET request and verifying response
        mockMvc.perform(MockMvcRequestBuilders.get("/email-templates/category/{category}", "testCategory")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "defaultSortBy")
                        .param("sortDir", "defaultSortDir")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                // Add additional assertions here based on your expected response
                // For example:
                // .andExpect(MockMvcResultMatchers.jsonPath("$.property").value("expectedValue"))
                .andReturn();
    }
}
