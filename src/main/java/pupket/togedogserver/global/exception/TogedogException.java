package pupket.togedogserver.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TogedogException extends RuntimeException {

    private final ExceptionCode exceptionCode;

    public TogedogException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }

    public TogedogException() {
        super(ExceptionCode.INVALID_PARAMETER.getMessage());
        this.exceptionCode = ExceptionCode.INVALID_PARAMETER;
    }

    public HttpStatus getHttpStatus() {
        return this.exceptionCode.getHttpStatus();
    }

    public String getErrorMessage() {
        return this.exceptionCode.getMessage();
    }
}
