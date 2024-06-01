package java03.team01.FAMS.model.payload.responseModel;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailSendResponse {
    private int No;
    private LocalDate sendDate;
    private String senderName;
    private String action;
}
