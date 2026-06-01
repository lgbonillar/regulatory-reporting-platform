package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.upload.infrastructure.UploadedFileRepository;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @Test
    void deleteThrowsExceptionForPathTraversalAttempt() {
        FileStorageService service = createService("/tmp/uploads", 10485760L);

        assertThatThrownBy(() -> service.delete("../etc/passwd"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid storage path");
    }

    @Test
    void loadAsResourceThrowsResourceNotFoundForInvalidPath() {
        FileStorageService service = createService("/tmp/uploads", 10485760L);

        assertThatThrownBy(() -> service.loadAsResource("../etc/passwd"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Invalid storage path");
    }

    @Test
    void loadAsResourceThrowsResourceNotFoundWhenFileDoesNotExist() {
        FileStorageService service = createService("/tmp/uploads", 10485760L);

        assertThatThrownBy(() -> service.loadAsResource("nonexistent/file.xlsx"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Uploaded file not found");
    }

    @Test
    void resolvePathThrowsResourceNotFoundForInvalidPath() {
        FileStorageService service = createService("/tmp/uploads", 10485760L);

        assertThatThrownBy(() -> service.resolvePath("../etc/passwd"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Invalid storage path");
    }

    @Test
    void resolvePathThrowsResourceNotFoundWhenFileDoesNotExist() {
        FileStorageService service = createService("/tmp/uploads", 10485760L);

        assertThatThrownBy(() -> service.resolvePath("nonexistent/file.xlsx"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Uploaded file not found");
    }

    private FileStorageService createService(String uploadDir, long maxFileSize) {
        return new FileStorageService(
                uploadDir,
                maxFileSize,
                uploadedFileRepository
        );
    }
}