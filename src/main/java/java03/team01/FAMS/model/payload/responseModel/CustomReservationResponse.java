package java03.team01.FAMS.model.payload.responseModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomReservationResponse {
//    private Long id;

    private CustomStudentResponse student;

    private CustomClassResponse classObj;

    private List<String> moduleName;

    private String reason;

    private LocalDate startDate;

    private LocalDate endDate;
}
