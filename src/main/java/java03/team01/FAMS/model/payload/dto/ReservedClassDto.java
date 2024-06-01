package java03.team01.FAMS.model.payload.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservedClassDto {
//    private Long id;

    @NotNull(message = "Student id is required")
    private Long studentId;

    @NotNull(message = "Class id is required")
    private Long classId;

    @NotEmpty(message = "Reason is required")
    private String reason;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}
