package java03.team01.FAMS.service;



import java03.team01.FAMS.model.payload.dto.EmailSendDto;
import java03.team01.FAMS.model.payload.dto.EmailTemplateDto;
import java03.team01.FAMS.model.payload.dto.EmailTemplateUpdateDto;
import java03.team01.FAMS.model.payload.responseModel.CustomReservationEmailResponse;
import java03.team01.FAMS.model.payload.responseModel.EmailSendResponse;
import java03.team01.FAMS.model.payload.responseModel.EmailTemplatesResponse;

import java.util.List;

public interface EmailService {
    EmailTemplateDto createEmailTemplate(EmailTemplateDto emailTemplateDto);

    EmailTemplateDto getEmailTemplate(Long id);

    EmailTemplatesResponse getAllEmailTemplate(int pageNo, int pageSize, String sortBy, String sortDir);

    EmailTemplatesResponse getAllEmailTemplateByCategory(String category,int pageNo, int pageSize, String sortBy, String sortDir);

    EmailTemplateDto updateEmailTemplate(Long id, EmailTemplateUpdateDto emailTemplateDto);

    EmailTemplateDto updateEmailTemplateStatus(Long id, String status);

    String sendEmail(EmailSendDto emailSendDto);
    CustomReservationEmailResponse getReservationEmailSend(int pageNo, int pageSize, String sortBy, String sortDir);



}
