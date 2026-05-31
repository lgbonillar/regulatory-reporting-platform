package dev.lgbonillar.regreporting.upload.application.support;

import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserRole;

public final class UploadedFileAccessRules {

    private UploadedFileAccessRules() {
    }

    public static boolean canListAs(User currentUser, User targetUser) {
        if (currentUser.hasRole(UserRole.ROOT) ||
                currentUser.hasRole(UserRole.ADMINISTRATOR)) {
            return true;
        }
        if (currentUser.hasRole(UserRole.ANALYST)) {
            return currentUser.getId().equals(targetUser.getId());
        }
        return false;
    }

    public static boolean canView(User currentUser, UploadedFile uploadedFile) {
        if (currentUser.hasRole(UserRole.ROOT) ||
                currentUser.hasRole(UserRole.ADMINISTRATOR)) {
            return true;
        }

        if (currentUser.hasRole(UserRole.ANALYST)) {
            return uploadedFile.getUploadedBy()
                    .getUsername()
                    .equals(currentUser.getUsername());
        }

        return false;
    }

    public static boolean isViewableStatus(UploadedFileStatus status) {
        return switch (status) {
            case STORED, PENDING_CORRECTION -> true;
            case MISSING, FAILED, DELETED, PENDING_VALIDATION -> false;
        };
    }

    public static boolean isDownloadableStatus(UploadedFileStatus status) {
        return switch (status) {
            case STORED, PENDING_CORRECTION -> true;
            case MISSING, FAILED, DELETED, PENDING_VALIDATION -> false;
        };
    }
}