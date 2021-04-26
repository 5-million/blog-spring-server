package pooro.blog.exception.post;

import pooro.blog.error.ErrorCode;
import pooro.blog.exception.CustomException;

public class PostNotExistException extends CustomException {
    private static final long serialVersionUID = -2116671122895194101L;

    public PostNotExistException() {
        super(ErrorCode.POST_NOT_EXIST);
    }
}
