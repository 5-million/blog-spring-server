package eol_g.blog.exception.category;

import eol_g.blog.error.ErrorCode;
import eol_g.blog.exception.CustomException;

public class CategoryDuplicateException extends CustomException {
    private static final long serialVersionUID = -2116671122895194101L;

    public CategoryDuplicateException() {
        super(ErrorCode.CATEGORY_DUPLICATE);
    }
}
