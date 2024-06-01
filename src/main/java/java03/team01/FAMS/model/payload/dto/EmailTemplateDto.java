package java03.team01.FAMS.model.payload.dto;



import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateDto {
    @NotEmpty(message = "Type should not be empty!")
    @Pattern(regexp = "^(STUDENT|TRAINER)$", message = "Type should be either STUDENT or TRAINER")
    private String type;
    @NotEmpty(message = "Name should not be empty!")
    private String name;
    @NotEmpty(message = "Description should not be empty!")
    private String description;
    private LocalDate createdDate;
    private String createdBy;
    @NotEmpty(message = "Category should not be empty!")
    @Pattern(regexp = "^(Inform|Remind|Score|Reservation)$", message = "Category should be Inform,Remind,Score,Reservation")
    private String category;
    private LocalDate updatedDate;
    private String updatedBy;
    private boolean status;
}
