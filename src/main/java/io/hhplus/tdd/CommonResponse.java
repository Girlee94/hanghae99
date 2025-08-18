package io.hhplus.tdd;

import lombok.Getter;

@Getter
public class CommonResponse<T> {

    private final String code;
    private final String message;
    private final T data;

    private CommonResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>("0000", "성공", data);
    }

    public static <T> CommonResponse<T> success() {
        return new CommonResponse<>("0000", "성공", null);
    }

    public static <T> CommonResponse<T> fail(String code, String message) {
        return new CommonResponse<>(code, message, null);
    }
}
