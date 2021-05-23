package eol_g.blog.exception.post;

import eol_g.blog.error.ErrorCode;
import eol_g.blog.exception.CustomException;

/**
 * 포스트가 임시저장 상태가 아닐 경우 발생하는 예외
 */
public class PostNotTempException extends CustomException {
    private static final long serialVersionUID = -2116671122895194101L;

    public PostNotTempException() {
        super(ErrorCode.POST_NOT_TEMP);
    }
}
