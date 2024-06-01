package java03.team01.FAMS.model.payload.responseModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomAssignResponse {
    private Long id;
    private String assignType;
    private String assignName;
    private Set<Float> assignScore;

}
