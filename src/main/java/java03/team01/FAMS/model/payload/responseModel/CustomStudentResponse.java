package java03.team01.FAMS.model.payload.responseModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomStudentResponse {
    private Long id;
    private String fullName;
    private String gender;
    private LocalDate dob;
    private String address;
    private String status;
}
