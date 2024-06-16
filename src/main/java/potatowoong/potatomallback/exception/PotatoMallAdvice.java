package potatowoong.potatomallback.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Slf4j
public class PotatoMallAdvice {

    /**
     * Handle CustomException
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseEntity> handleCustomException(CustomException e) {
        return ErrorResponseEntity.toResponseEntity(e.getErrorCode());
    }

    /**
     * Handle MethodArgumentNotValidException
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseEntity> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return ResponseEntity.ok(ErrorResponseEntity.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .name("Validation Failed")
            .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message(e.getBindingResult().getAllErrors().get(0).getDefaultMessage())
            .build());
    }

    /**
     * Handle MaxUploadSizeExceededException
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseEntity> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return ErrorResponseEntity.toResponseEntity(ErrorCode.EXCEEDED_FILE_SIZE);
    }

    /**
     * Handle Exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseEntity> handleException(Exception e) {
        log.error("[Exception] :: ", e);
        return ErrorResponseEntity.toResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
