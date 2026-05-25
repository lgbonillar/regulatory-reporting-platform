package dev.lgbonillar.regreporting.shared;

import java.time.Instant;

public record ApiMetadata(
        Instant timestamp,
        Integer count,
        String requestId
) {

    public static ApiMetadata now() {
        return new ApiMetadata(Instant.now(), null, null);
    }

    public static ApiMetadata withCount(int count) {
        return new ApiMetadata(Instant.now(), count, null);
    }

}
