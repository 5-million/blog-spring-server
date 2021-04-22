package pooro.blog.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    POST_DUPLICATE(400, "P001", "이미 존재하는 포스트입니다."),
    CATEGORY_NOT_FOUND(400, "C001", "존재하지 않는 카테고리입니다.");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
