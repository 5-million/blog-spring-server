package eol_g.blog.exception;

import eol_g.blog.error.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    private ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
