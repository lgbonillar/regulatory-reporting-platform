package com.mrcrafterman.regreporting.upload.domain;

import com.mrcrafterman.regreporting.shared.BusinessConflictException;
import com.mrcrafterman.regreporting.users.domain.User;
import com.mrcrafterman.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UploadedFileTest {

    @Test
    void storedFileCanBeProcessed() {
        UploadedFile file = uploadedFile(UploadedFileStatus.STORED);

        assertThat(file.canBeProcessed()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = UploadedFileStatus.class, names = {"DELETED", "MISSING", "FAILED"})
    void unavailableFileCannotBeProcessed(UploadedFileStatus status) {
        UploadedFile file = uploadedFile(status);

        assertThat(file.canBeProcessed()).isFalse();
        assertThatThrownBy(file::ensureCanBeProcessed)
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining(status.name());
    }

    @Test
    void replaceWithRestoresStoredStatus() {
        UploadedFile file = uploadedFile(UploadedFileStatus.FAILED);

        file.replaceWith(
                "new-stored.xlsx",
                "/uploads/new-stored.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                2048L,
                "new-checksum"
        );

        assertThat(file.getStatus()).isEqualTo(UploadedFileStatus.STORED);
        assertThat(file.getStoredFilename()).isEqualTo("new-stored.xlsx");
        assertThat(file.getStoragePath()).isEqualTo("/uploads/new-stored.xlsx");
        assertThat(file.getFileSize()).isEqualTo(2048L);
        assertThat(file.getChecksum()).isEqualTo("new-checksum");
        assertThat(file.getUpdatedAt()).isNotNull();
    }

    @Test
    void markDeletedChangesStatusAndUpdatesTimestamp() {
        UploadedFile file = uploadedFile(UploadedFileStatus.STORED);

        file.markDeleted();

        assertThat(file.getStatus()).isEqualTo(UploadedFileStatus.DELETED);
        assertThat(file.getUpdatedAt()).isNotNull();
    }

    private UploadedFile uploadedFile(UploadedFileStatus status) {
        return new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "/uploads/stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                1024L,
                "checksum",
                status,
                user()
        );
    }

    private User user() {
        return new User(
                "analyst01",
                "analyst01@example.com",
                "Analyst 01",
                null,
                false,
                UserStatus.ACTIVE
        );
    }

}
