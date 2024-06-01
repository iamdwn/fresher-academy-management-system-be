package java03.team01.FAMS.model.payload.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplateUpdateDto {

    @Pattern(regexp = "^(STUDENT|TRAINER)$", message = "Type should be either STUDENT or TRAINER")
    private String type;
    private String name;
    private String description;
    private LocalDate createdDate;
    private String createdBy;
    @Pattern(regexp = "^(Inform|Remind|Score|Reservation)$", message = "Category should be Inform,Remind,Score,Reservation")
    private String category;
    private LocalDate updatedDate;
    private String updatedBy;
    private boolean status;
}
