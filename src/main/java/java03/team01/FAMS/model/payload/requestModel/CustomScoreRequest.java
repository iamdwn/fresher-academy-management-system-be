package java03.team01.FAMS.model.payload.requestModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomScoreRequest {
//        private Long classId;
//    private Long studentId;
//    private Long moduleId;
//    private Long assignmentId;
    private Long scoreId;
    private Float scoreValue;
}
