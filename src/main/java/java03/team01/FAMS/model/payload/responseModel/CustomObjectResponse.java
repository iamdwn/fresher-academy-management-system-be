package java03.team01.FAMS.model.payload.responseModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomObjectResponse {
    private String status;
    private String message;
    private Object data;
}
