package java03.team01.FAMS.model.payload.requestModel;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomCertificateRequest {
    private Long studentId;
    private String certificateStatus;
    private Date certificateDate;
}
