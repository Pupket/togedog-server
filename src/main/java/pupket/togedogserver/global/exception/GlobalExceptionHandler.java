package pupket.togedogserver.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    Logger defaultLogger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    Logger exceptionLogger = LoggerFactory.getLogger("ExceptionLogger");

    @ExceptionHandler(TogedogException.class)
    public ResponseEntity<ExceptionResponse> handleAlleException(TogedogException ex) {
        defaultLogger.warn(ex.getMessage());
        exceptionLogger.warn(ex.getMessage(), ex);

        ExceptionResponse exceptionResponse = ExceptionResponse.fromException(ex.getExceptionCode());

        if(exceptionResponse.httpStatus().equals(HttpStatus.CONFLICT)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exceptionResponse);
        }

        return ResponseEntity.status(exceptionResponse.httpStatus()).body(exceptionResponse);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ExceptionResponse> handleDefaultException(Exception ex) {
        defaultLogger.error(ex.getMessage(), ex);
        exceptionLogger.error(ex.getMessage(), ex);

        ExceptionResponse exceptionResponse = ExceptionResponse.fromError(ex);
        return ResponseEntity.status(exceptionResponse.httpStatus()).body(exceptionResponse);
    }

}
