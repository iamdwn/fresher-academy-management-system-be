package java03.team01.FAMS.model.payload.responseModel;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ImportExcelResponse {
    private List<String> successList;
    private List<String> failList;
}
