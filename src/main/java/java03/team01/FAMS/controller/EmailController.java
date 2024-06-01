package java03.team01.FAMS.controller;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java03.team01.FAMS.model.entity.EmailTemplate;
import java03.team01.FAMS.model.payload.dto.EmailSendDto;
import java03.team01.FAMS.model.payload.dto.EmailTemplateDto;
import java03.team01.FAMS.model.payload.dto.EmailTemplateUpdateDto;
import java03.team01.FAMS.model.payload.responseModel.CustomReservationEmailResponse;
import java03.team01.FAMS.model.payload.responseModel.EmailTemplatesResponse;
import java03.team01.FAMS.service.EmailService;
import java03.team01.FAMS.utils.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/email")
public class EmailController {
    @Autowired
    private EmailService emailService;

    @SecurityRequirement(name = "Bear Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/email-templates")
    public ResponseEntity<?> createEmailTemplate(@Valid @RequestBody EmailTemplateDto emailTemplateDto){
        EmailTemplateDto emailTemplateDto1 = emailService.createEmailTemplate(emailTemplateDto);
        return new ResponseEntity<>(emailTemplateDto1, HttpStatus.CREATED);
    }

    @SecurityRequirement(name = "Bear Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/email-templates")
    public EmailTemplatesResponse getAllEmailTemp(
            @RequestParam(value = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir
    ){
        return emailService.getAllEmailTemplate(pageNo, pageSize, sortBy, sortDir);
    }

    @SecurityRequirement(name = "Bear Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/email-templates/category/{category}")
    public EmailTemplatesResponse getAllEmailTempByCategory(
            @PathVariable("category") String category,
            @RequestParam(value = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir
    ){
        return emailService.getAllEmailTemplateByCategory(category,pageNo, pageSize, sortBy, sortDir);
    }

    @SecurityRequirement(name = "Bear Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/email-templates/{id}")
    public ResponseEntity<?> getAllEmailTemplates(@PathVariable("id") Long id){
        EmailTemplateDto emailTemplate = emailService.getEmailTemplate(id);
        return new ResponseEntity<>(emailTemplate, HttpStatus.OK);
    }

    @SecurityRequirement(name = "Bear Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/email-templates/{id}")
    public ResponseEntity<?> updateEmailTemplate(@PathVariable("id") Long id, @Valid @RequestBody EmailTemplateUpdateDto emailTemplateDto){
        EmailTemplateDto emailTemplate = emailService.updateEmailTemplate(id, emailTemplateDto);
        return new ResponseEntity<>(emailTemplate, HttpStatus.OK);
    }

    @SecurityRequirement(name = "Bear Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/email-templates/{id}/{status}")
    public ResponseEntity<?> updateEmailTemplateStatus(@PathVariable("id") Long id, @PathVariable("status") String status){
        EmailTemplateDto emailTemplate = emailService.updateEmailTemplateStatus(id, status);
        return new ResponseEntity<>(emailTemplate, HttpStatus.OK);
    }

    @SecurityRequirement(name = "Bear Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/sendMail")
    public String sendMail(@RequestBody EmailSendDto emailSendDto)
    {
        return emailService.sendEmail(emailSendDto);
    }



    @SecurityRequirement(name = "Bear Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/history")
    public CustomReservationEmailResponse viewHistoryEmail(
            @RequestParam(value = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir
    ){
        return emailService.getReservationEmailSend(pageNo, pageSize, sortBy, sortDir);
    }

}
