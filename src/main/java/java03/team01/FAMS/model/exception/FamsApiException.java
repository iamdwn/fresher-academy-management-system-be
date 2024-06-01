package java03.team01.FAMS.model.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class FamsApiException extends RuntimeException{
    @Getter
    private HttpStatus status;

    @Getter
    private String message;

    public FamsApiException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public FamsApiException(String message, HttpStatus status, String message1) {
        super(message);
        this.status = status;
        this.message = message1;
    }
}
