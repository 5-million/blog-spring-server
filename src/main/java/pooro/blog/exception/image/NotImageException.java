package pooro.blog.exception.image;

import pooro.blog.error.ErrorCode;
import pooro.blog.exception.CustomException;

public class NotImageException extends CustomException {
    private static final long serialVersionUID = -2116671122895194101L;

    public NotImageException() {
        super(ErrorCode.NOT_IMAGE);
    }
}
