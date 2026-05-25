package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileResponse;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UploadedFileMapperTest {

    private final UploadedFileMapper uploadedFileMapper = new UploadedFileMapper();

    @Test
    void mapsUploadedFileToResponse() {
        UploadedFile uploadedFile = new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "/uploads/stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                1024L,
                "checksum",
                UploadedFileStatus.STORED,
                analyst()
        );

        UploadedFileResponse result = uploadedFileMapper.toUploadedFileResponse(uploadedFile);

        assertThat(result.fileId()).isEqualTo(uploadedFile.getId());
        assertThat(result.originalFilename()).isEqualTo("report.xlsx");
        assertThat(result.storedFilename()).isEqualTo("stored-report.xlsx");
        assertThat(result.contentType()).isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                assertThat(result.fileSize()).isEqualTo(1024L);
        assertThat(result.checksum()).isEqualTo("checksum");
        assertThat(result.fileStatus()).isEqualTo(UploadedFileStatus.STORED.name());
        assertThat(result.uploadedBy()).isEqualTo("analyst01");
        assertThat(result.uploadedAt()).isEqualTo(uploadedFile.getUploadedAt());
        assertThat(result.updatedAt()).isEqualTo(uploadedFile.getUpdatedAt());
    }

    private User analyst() {
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
