package eol_g.blog.exception.post;

import eol_g.blog.error.ErrorCode;
import eol_g.blog.exception.CustomException;

public class PostNotExistException extends CustomException {
    private static final long serialVersionUID = -2116671122895194101L;

    public PostNotExistException() {
        super(ErrorCode.POST_NOT_EXIST);
    }
}
