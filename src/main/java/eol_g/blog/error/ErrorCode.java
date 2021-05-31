package eol_g.blog.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    POST_DUPLICATE(400, "P001", "이미 존재하는 포스트입니다.", "이미 존재하는 포스터"),
    POST_NOT_FOUND(500,"P002", "포스트를 찾을 수 없습니다.", "등록된 포스트이지만 해당 파일을 찾지 못하는 경우"),
    POST_NOT_EXIST(404, "P003", "존재하지 않는 포스트입니다.", "등록되지 않은 포스터인 경우"),
    POST_NOT_TEMP(400, "P004", "임시저장된 포스트가 아닙니다.", "이미 공개된 포스터인 경우"),
    CATEGORY_DUPLICATE(400, "C001", "이미 존재하는 카테고리입니다.", "이미 등록된 카테고리"),
    CATEGORY_NOT_EXIST(404, "C002", "존재하지 않는 카테고리입니다.", "등록되지 않은 카테고리인 경우"),
    NOT_IMAGE(400, "I001", "이미지 파일이 아닙니다.", "파일 확장자가 이미지 형식이 아닌 경우");

    private final int status;
    private final String code;
    private final String message;
    private final String description;

    ErrorCode(int status, String code, String message, String description) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.description = description;
    }
}
