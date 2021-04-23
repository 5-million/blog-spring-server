package pooro.blog.exception.category;

import pooro.blog.error.ErrorCode;
import pooro.blog.exception.CustomException;

public class CategoryDuplicateException extends CustomException {
    private static final long serialVersionUID = -2116671122895194101L;

    public CategoryDuplicateException() {
        super(ErrorCode.CATEGORY_DUPLICATE);
    }
}
