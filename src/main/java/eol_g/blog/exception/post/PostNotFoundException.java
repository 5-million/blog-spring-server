package eol_g.blog.exception.post;

import eol_g.blog.error.ErrorCode;
import eol_g.blog.exception.CustomException;

/**
 * 포스트가 존재하지 않을 경우 발생하는 예외
 */
public class PostNotFoundException extends CustomException {
    private static final long serialVersionUID = -2116671122895194101L;

    public PostNotFoundException() {
        super(ErrorCode.POST_NOT_FOUND);
    }
}
