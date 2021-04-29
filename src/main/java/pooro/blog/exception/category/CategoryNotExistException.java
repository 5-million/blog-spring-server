package pooro.blog.exception.category;

import pooro.blog.error.ErrorCode;
import pooro.blog.exception.CustomException;

public class CategoryNotExistException extends CustomException {
    private static final long serialVersionUID = -2116671122895194101L;

    public CategoryNotExistException() {
        super(ErrorCode.CATEGORY_NOT_EXIST);
    }
}
