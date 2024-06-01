package java03.team01.FAMS.model.payload.responseModel;

import lombok.*;
import org.apache.xmlbeans.impl.xb.xsdschema.ListDocument;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CustomScoreResponse {
    private Long id;
    private String fullName;
    private String account;
    private List<CustomModuleResponse> module;

    public double getAverageModuleScore() {
        if (module == null || module.isEmpty()) {
            return 0.0;
        }
        return module.stream()
                .mapToDouble(CustomModuleResponse::getModuleScore)
                .average()
                .orElse(0.0);
    }
}
