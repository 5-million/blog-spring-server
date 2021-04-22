package pooro.blog.exception;

import lombok.Getter;
import pooro.blog.error.ErrorCode;

@Getter
public class CustomException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    private ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
