package dev.lgbonillar.regreporting.shared;

import java.util.Collection;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        ApiMetadata metadata
) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, ApiMetadata.now());
    }

    public static <T extends Collection<?>> ApiResponse<T> successList(
            String message,
            T data
    ) {
        return new ApiResponse<>(true, message, data, ApiMetadata.withCount(data.size()));
    }

}
