package java03.team01.FAMS.service.impl;

import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import java03.team01.FAMS.model.entity.*;
import java03.team01.FAMS.model.exception.FamsApiException;
import java03.team01.FAMS.model.payload.dto.*;
import java03.team01.FAMS.model.payload.responseModel.CustomReservationEmailResponse;
import java03.team01.FAMS.model.payload.responseModel.CustomReservationResponse;
import java03.team01.FAMS.model.payload.responseModel.EmailSendResponse;
import java03.team01.FAMS.model.payload.responseModel.EmailTemplatesResponse;
import java03.team01.FAMS.repository.EmailSendRepository;
import java03.team01.FAMS.repository.EmailTemplateRepository;
import java03.team01.FAMS.repository.StudentRepository;
import java03.team01.FAMS.repository.UserRepository;
import java03.team01.FAMS.service.EmailService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmailServiceImp implements EmailService {

    @Autowired
    private EmailTemplateRepository emailTemplateRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailSendRepository emailSendRepository;


    @Autowired
    private StudentRepository studentRepository;


    @Value("${spring.mail.username}") private String sender;

    @Override
    public EmailTemplateDto createEmailTemplate(EmailTemplateDto emailTemplateDto) {

        EmailTemplate emailTemplate = modelMapper.map(emailTemplateDto, EmailTemplate.class);
        emailTemplate.setCreatedDate(LocalDate.now());
        emailTemplate.setUpdatedDate(LocalDate.now());
        emailTemplate.setUpdatedBy(emailTemplate.getCreatedBy());
        emailTemplate.setStatus(true);
        return modelMapper.map( emailTemplateRepository.save(emailTemplate), EmailTemplateDto.class);
    }

    @Override
    public EmailTemplateDto getEmailTemplate(Long id) {
        return modelMapper.map(emailTemplateRepository.findById(id), EmailTemplateDto.class);
    }

    @Override
    public EmailTemplatesResponse getAllEmailTemplate(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<EmailTemplate> emailTemplates = emailTemplateRepository.findAll(pageable);

        // get content for page object
        List<EmailTemplate> listOfEmailTemplate = emailTemplates.getContent();

        List<EmailTemplateDto> content= listOfEmailTemplate.stream().map(template -> modelMapper.map(template, EmailTemplateDto.class)).collect(Collectors.toList());

        EmailTemplatesResponse templatesResponse = new EmailTemplatesResponse();
        templatesResponse.setContent(content);
        templatesResponse.setPageNo(emailTemplates.getNumber());
        templatesResponse.setPageSize(emailTemplates.getSize());
        templatesResponse.setTotalElements(emailTemplates.getTotalElements());
        templatesResponse.setTotalPages(emailTemplates.getTotalPages());
        templatesResponse.setLast(emailTemplates.isLast());

        return templatesResponse;
    }

    @Override
    public EmailTemplatesResponse getAllEmailTemplateByCategory(String category, int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // create Pageable instance
        Page<EmailTemplate> emailTemplates;

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        if (category.equalsIgnoreCase("Inform")
                ||category.equalsIgnoreCase("Remind")
                ||category.equalsIgnoreCase("Score")
                ||category.equalsIgnoreCase("Reservation"))
        {
            emailTemplates = emailTemplateRepository.findByCategory(category,pageable);}
        else {
            emailTemplates = emailTemplateRepository.findAll(pageable);

        }

        // get content for page object
        List<EmailTemplate> listOfEmailTemplate = emailTemplates.getContent();


        List<EmailTemplateDto> content= listOfEmailTemplate.stream().map(template -> modelMapper.map(template, EmailTemplateDto.class)).collect(Collectors.toList());

        EmailTemplatesResponse templatesResponse = new EmailTemplatesResponse();
        templatesResponse.setContent(content);
        templatesResponse.setPageNo(emailTemplates.getNumber());
        templatesResponse.setPageSize(emailTemplates.getSize());
        templatesResponse.setTotalElements(emailTemplates.getTotalElements());
        templatesResponse.setTotalPages(emailTemplates.getTotalPages());
        templatesResponse.setLast(emailTemplates.isLast());

        return templatesResponse;

    }

    @Override
    public EmailTemplateDto updateEmailTemplate(Long id, EmailTemplateUpdateDto emailTemplateDto) {
        Optional<EmailTemplate> emailTemplate = emailTemplateRepository.findById(id);
        if (emailTemplate.isEmpty()) {
            throw new FamsApiException(HttpStatus.BAD_REQUEST,"Email template not found with ID: " + id);
        }

        EmailTemplate existingTemplate = emailTemplate.get();
        existingTemplate.setName(emailTemplateDto.getName() != null ? emailTemplateDto.getName() : existingTemplate.getName());
        existingTemplate.setType(emailTemplateDto.getType() != null ? emailTemplateDto.getType() : existingTemplate.getType());
        existingTemplate.setDescription(emailTemplateDto.getDescription() != null ? emailTemplateDto.getDescription() : existingTemplate.getDescription());
        existingTemplate.setCategory(emailTemplateDto.getCategory() != null ? emailTemplateDto.getCategory() : existingTemplate.getCategory());
        existingTemplate.setUpdatedDate(LocalDate.now());

        EmailTemplate updatedTemplate = emailTemplateRepository.save(existingTemplate);

        return modelMapper.map(updatedTemplate, EmailTemplateDto.class);
    }

    @Override
    public EmailTemplateDto updateEmailTemplateStatus(Long id, String status) {
        Optional<EmailTemplate> emailTemplate = emailTemplateRepository.findById(id);
        if (emailTemplate.isEmpty()) {
            throw new FamsApiException(HttpStatus.BAD_REQUEST,"Email template not found with ID: " + id);
        }
        EmailTemplate existingTemplate = emailTemplate.get();
        if(status.equalsIgnoreCase("inactive")){
            existingTemplate.setStatus(false);
        }else if(status.equalsIgnoreCase("active")){
            existingTemplate.setStatus(true);
        }else{
            throw new FamsApiException(HttpStatus.BAD_REQUEST,"Status type not valid.");
        }
        EmailTemplate updatedTemplate = emailTemplateRepository.save(existingTemplate);
        return modelMapper.map(updatedTemplate, EmailTemplateDto.class);

    }

    @Override
    public String sendEmail(EmailSendDto emailSendDto) {
        try {
            EmailSend emailSend = new EmailSend();
            User sender1 = userRepository.findById(emailSendDto.getSenderId()).orElse(null);
            if ((sender1 == null) || sender1.getRole().getId()!=2) {
                throw new FamsApiException(HttpStatus.BAD_REQUEST,"Sender not found for ID not match role: " + emailSendDto.getSenderId());
            }
            emailSend.setSender(sender1);

            EmailTemplate template = emailTemplateRepository.findById(emailSendDto.getTemplateId()).orElse(null);
            if (template == null || !template.isStatus()) {
                throw new FamsApiException( HttpStatus.BAD_REQUEST,"Template not found for ID: " + emailSendDto.getTemplateId()+" or Template is inactive");
            }
            if(template.getType().equalsIgnoreCase("Trainer") && emailSendDto.getReceiverType().equalsIgnoreCase("Student")){
                throw new FamsApiException( HttpStatus.BAD_REQUEST,"Type of template is invalid.");

            }
            if(template.getType().equalsIgnoreCase("Student") && emailSendDto.getReceiverType().equalsIgnoreCase("User")){
                throw new FamsApiException( HttpStatus.BAD_REQUEST,"Type of template is invalid.");
            }
            emailSend.setEmailTemplate(template);
            emailSend.setContent(emailSendDto.getContent());
            emailSend.setSendDate(LocalDate.now());
            emailSend.setReceiverType(emailSendDto.getReceiverType().toUpperCase());

            if (emailSendDto.getReceiverType().equalsIgnoreCase("user")) {
                Set<EmailSendUser> emailSendUsers = createEmailSendUsers(emailSendDto.getEmailSendUsers(), emailSend);
                emailSend.setEmailSendUsers(emailSendUsers);
                emailSend = emailSendRepository.save(emailSend);

            } else if (emailSendDto.getReceiverType().equalsIgnoreCase("student")) {
                Set<EmailSendStudent> emailSendStudents = createEmailSendStudent(emailSendDto.getEmailSendStudents(), emailSend);
                emailSend.setEmailSendStudents(emailSendStudents);
                emailSend = emailSendRepository.save(emailSend);

            } else if (emailSendDto.getReceiverType().equalsIgnoreCase("both")) {
                Set<EmailSendUser> emailSendUsers = createEmailSendUsers(emailSendDto.getEmailSendUsers(), emailSend);
                Set<EmailSendStudent> emailSendStudents = createEmailSendStudent(emailSendDto.getEmailSendStudents(), emailSend);

                emailSend.setEmailSendUsers(emailSendUsers);
                emailSend.setEmailSendStudents(emailSendStudents);
                emailSend = emailSendRepository.save(emailSend);

            } else {
                throw new FamsApiException(HttpStatus.BAD_REQUEST, "Type not match.");
            }


            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("${spring.mail.username}");
            if (emailSend.getEmailSendUsers() != null) {
                for (EmailSendUser emailSendUser : emailSend.getEmailSendUsers()) {
                    // Determine content for each user
                    String emailTemplateDescription = emailSend.getEmailTemplate().getDescription();
                    emailTemplateDescription = emailTemplateDescription
                            .replace("[User]", emailSendUser.getUser().getFullName() != null ? emailSendUser.getUser().getFullName() : "")
                            .replace("[Admin]", sender1.getFullName() != null ? sender1.getFullName() : "")
                            .replace("[User.Phone]", emailSendUser.getUser().getPhone() != null ? emailSendUser.getUser().getPhone() : "")
                            .replace("[Admin.Phone]", sender1.getPhone() != null ? sender1.getPhone() : "")
                            .replace("[User/Student]", emailSendUser.getUser().getFullName() != null ? emailSendUser.getUser().getFullName() : "")
                            .replace("[User/Student.Phone]", emailSendUser.getUser().getPhone() != null ? emailSendUser.getUser().getPhone() : "")
                            .replace("[Content]", emailSend.getContent() != null ? emailSend.getContent() : "");


                    // Create a MimeMessageHelper for each user
                    helper.setTo(emailSendUser.getUser().getEmail());
                    helper.setSubject(emailSend.getEmailTemplate().getName());
                    helper.setText(emailTemplateDescription, true);
                    // Send the message
                    mailSender.send(message);
                }
            }

            if (emailSend.getEmailSendStudents() != null) {
                for (EmailSendStudent emailSendStudent : emailSend.getEmailSendStudents()) {
                    // Determine content for each user
                    String emailTemplateDescription = emailSend.getEmailTemplate().getDescription();
                    emailTemplateDescription = emailTemplateDescription
                            .replace("[Student]", emailSendStudent.getStudent().getFullName() != null ? emailSendStudent.getStudent().getFullName() : "")
                            .replace("[Admin]", sender1.getFullName() != null ? sender1.getFullName() : "")
                            .replace("[Student.Phone]", emailSendStudent.getStudent().getPhone() != null ? emailSendStudent.getStudent().getPhone() : "")
                            .replace("[Admin.Phone]", sender1.getPhone() != null ? sender1.getPhone() : "")
                            .replace("[User/Student]", emailSendStudent.getStudent().getFullName() != null ? emailSendStudent.getStudent().getFullName() : "")
                            .replace("[User/Student.Phone]", emailSendStudent.getStudent().getPhone() != null ? emailSendStudent.getStudent().getPhone() : "")
                            .replace("[Content]", emailSend.getContent() != null ? emailSend.getContent() : "");


                    // Create a MimeMessageHelper for each user
                    helper.setTo(emailSendStudent.getStudent().getEmail());
                    helper.setSubject(emailSend.getEmailTemplate().getName());
                    helper.setText(emailTemplateDescription, true);
                    // Send the message
                    mailSender.send(message);
                }
            }
            return "Mail Sent Successfully...";
        } catch (Exception e) {
            return "Error while Sending Mail: " + e.getMessage();
        }
    }

    @Override
    public CustomReservationEmailResponse getReservationEmailSend(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<EmailSend> emailSends = emailSendRepository.findAll(pageable);

        // get content for page object
        List<EmailSend> listOfEmailSend = emailSends.getContent();

        List<EmailSendResponse> emailSendResponseList = new ArrayList<>();
        int index =1;
        for (EmailSend emailSend : listOfEmailSend){
            EmailSendResponse response = new EmailSendResponse();
            response.setNo(index++);
            response.setSendDate(emailSend.getSendDate());
            response.setSenderName(emailSend.getSender().getFullName());
            if(emailSend.getEmailSendStudents().size() > 9 && emailSend.getReceiverType().equalsIgnoreCase("student")){
                response.setAction("Send email to multiple students.");
            }else if (emailSend.getEmailSendStudents().size() <= 9 && emailSend.getReceiverType().equalsIgnoreCase("student")  ) {
                StringBuilder actionBuilder = new StringBuilder("Send email to ");
                Set<EmailSendStudent> emailSendStudents = emailSend.getEmailSendStudents();
                Iterator<EmailSendStudent> iterator = emailSendStudents.iterator();
                while (iterator.hasNext()) {
                    EmailSendStudent emailSendStudent = iterator.next();
                    actionBuilder.append(emailSendStudent.getStudent().getEmail());
                    if (iterator.hasNext()) {
                        actionBuilder.append(", ");
                    }
                }
                actionBuilder.append(".");
                response.setAction(actionBuilder.toString());
            }else{
                response.setAction("Send email to all users.");
            }

            emailSendResponseList.add(response);
        }

        CustomReservationEmailResponse sendResponse= new CustomReservationEmailResponse();
        sendResponse.setContent(emailSendResponseList);
        sendResponse.setPageNo(emailSends.getNumber());
        sendResponse.setPageSize(emailSends.getSize());
        sendResponse.setTotalElements(emailSends.getTotalElements());
        sendResponse.setTotalPages(emailSends.getTotalPages());
        sendResponse.setLast(emailSends.isLast());

        return sendResponse;
    }

    public Set<EmailSendStudent> createEmailSendStudent(Set<EmailSendStudentDto> emailSendStudentDtos, EmailSend emailSend) {
        Set<EmailSendStudent> emailSendStudents = new HashSet<>();

        for (EmailSendStudentDto studentDto : emailSendStudentDtos) {
            // Assuming you have a UserRepository for fetching User entities
            Student st = studentRepository.findById(studentDto.getStudentId()).orElse(null);

            if (st != null) {
                // Create EmailSendUser entity and associate it with EmailSend and User
                EmailSendStudent emailSendStudent = new EmailSendStudent();
                emailSendStudent.setEmailSend(emailSend);
                emailSendStudent.setStudent(st);

                // Add the EmailSendUser entity to the set
                emailSendStudents.add(emailSendStudent);
            }
        }
        return emailSendStudents;
    }

    public Set<EmailSendUser> createEmailSendUsers(Set<EmailSendUserDto> emailSendUserDtos, EmailSend emailSend) {
        Set<EmailSendUser> emailSendUsers = new HashSet<>();

        for (EmailSendUserDto userDto : emailSendUserDtos) {
            // Assuming you have a UserRepository for fetching User entities
            User user = userRepository.findById(userDto.getUserId()).orElse(null);

            if (user != null) {
                // Create EmailSendUser entity and associate it with EmailSend and User
                EmailSendUser emailSendUser = new EmailSendUser();
                emailSendUser.setEmailSend(emailSend);
                emailSendUser.setUser(user);

                // Add the EmailSendUser entity to the set
                emailSendUsers.add(emailSendUser);
            }
        }

        return emailSendUsers;
    }





}
