package dev.lgbonillar.regreporting.shared;

public record ApiError(
        String code,
        Object details
) {
}
