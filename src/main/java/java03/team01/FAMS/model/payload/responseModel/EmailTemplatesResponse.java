package java03.team01.FAMS.model.payload.responseModel;

import java03.team01.FAMS.model.payload.dto.EmailTemplateDto;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class EmailTemplatesResponse {
    private List<EmailTemplateDto> content;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
