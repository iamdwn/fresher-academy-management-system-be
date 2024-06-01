package java03.team01.FAMS.model.payload.responseModel;

import java03.team01.FAMS.model.payload.dto.EmailTemplateDto;
import lombok.*;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomReservationEmailResponse {
    private List<EmailSendResponse> content;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
