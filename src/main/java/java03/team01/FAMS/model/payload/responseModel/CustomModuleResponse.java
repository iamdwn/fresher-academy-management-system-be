package java03.team01.FAMS.model.payload.responseModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomModuleResponse {

    private Long id;
    private String moduleName;
    private float moduleScore;
    private List<CustomAssignResponse> assign;
    private String status;
}
