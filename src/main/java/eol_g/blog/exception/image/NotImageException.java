package eol_g.blog.exception.image;

import eol_g.blog.error.ErrorCode;
import eol_g.blog.exception.CustomException;

public class NotImageException extends CustomException {
    private static final long serialVersionUID = -2116671122895194101L;

    public NotImageException() {
        super(ErrorCode.NOT_IMAGE);
    }
}
