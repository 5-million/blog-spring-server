package pooro.blog.exception.category;

import pooro.blog.error.ErrorCode;
import pooro.blog.exception.CustomException;

/**
 * 존재하지 않는 카테고리를 포함한 요청시 발생하는 예외
 */
public class CategoryNotFoundException extends CustomException {
    private static final long serialVersionUID = -2116671122895194101L;

    public CategoryNotFoundException() {
        super(ErrorCode.CATEGORY_NOT_FOUND);
    }
}
