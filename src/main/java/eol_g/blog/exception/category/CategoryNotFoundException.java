package eol_g.blog.exception.category;

import eol_g.blog.error.ErrorCode;
import eol_g.blog.exception.CustomException;

/**
 * 존재하지 않는 카테고리를 포함한 요청시 발생하는 예외
 */
public class CategoryNotFoundException extends CustomException {
    private static final long serialVersionUID = -2116671122895194101L;

    public CategoryNotFoundException() {
        super(ErrorCode.CATEGORY_NOT_FOUND);
    }
}
