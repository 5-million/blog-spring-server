package pooro.blog.exception.post;

import pooro.blog.error.ErrorCode;
import pooro.blog.exception.CustomException;

/**
 * 이미 존재하는 포스트 제목으로 업로드 요청시 발생하는 예외
 */
public class PostDuplicateException extends CustomException {
    private static final long serialVersionUID = -2116671122895194101L;

    public PostDuplicateException() {
        super(ErrorCode.POST_DUPLICATE);
    }
}
