package dev.lgbonillar.regreporting.shared;

public record ApiErrorResponse(
        boolean success,
        String message,
        ApiError error,
        ApiMetadata metadata
) {

    public static ApiErrorResponse of(String message, String code) {
        return new ApiErrorResponse(
                false,
                message,
                new ApiError(code, null),
                ApiMetadata.now()
        );
    }

}
