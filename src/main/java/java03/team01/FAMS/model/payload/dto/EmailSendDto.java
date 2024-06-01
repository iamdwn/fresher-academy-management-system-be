package java03.team01.FAMS.model.payload.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSendDto {
    @NotNull(message = "SenderID should not be empty!")
    private Long templateId;
    @NotNull(message = "SenderID should not be empty!")
    private Long senderId;
    private String content;
    //private LocalDate sendDate;
    @NotEmpty(message = "ReceiverType should not be empty!")
    private String receiverType;

    private Set<EmailSendStudentDto> emailSendStudents;
    private Set<EmailSendUserDto> emailSendUsers;
}
