package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.dto.DownloadedFile;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DownloadFileServiceTest {

    @Mock
    private UploadedFileQueryService uploadedFileQueryService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private MarkFileMissingService markFileMissingService;

    @InjectMocks
    private DownloadFileService downloadFileService;

    @Test
    void downloadFileReturnsFileWhenStatusIsStored() {
        UUID fileId = UUID.randomUUID();
        UploadedFile file = storedFile(fileId, UploadedFileStatus.STORED);
        Resource resource = new ByteArrayResource(new byte[]{1, 2, 3});

        when(uploadedFileQueryService.getViewableUploadedFile(fileId)).thenReturn(file);
        when(fileStorageService.loadAsResource(file.getStoragePath())).thenReturn(resource);

        DownloadedFile result = downloadFileService.downloadFile(fileId);

        assertThat(result.uploadedFile()).isSameAs(file);
        assertThat(result.resource()).isSameAs(resource);
    }

    @Test
    void downloadFileReturnsFileWhenStatusIsPendingCorrection() {
        UUID fileId = UUID.randomUUID();
        UploadedFile file = storedFile(fileId, UploadedFileStatus.PENDING_CORRECTION);
        Resource resource = new ByteArrayResource(new byte[]{1, 2, 3});

        when(uploadedFileQueryService.getViewableUploadedFile(fileId)).thenReturn(file);
        when(fileStorageService.loadAsResource(file.getStoragePath())).thenReturn(resource);

        DownloadedFile result = downloadFileService.downloadFile(fileId);

        assertThat(result.uploadedFile()).isSameAs(file);
    }

    @Test
    void downloadFileThrowsResourceNotFoundWhenStatusIsMissing() {
        UUID fileId = UUID.randomUUID();
        UploadedFile file = storedFile(fileId, UploadedFileStatus.MISSING);

        when(uploadedFileQueryService.getViewableUploadedFile(fileId)).thenReturn(file);

        assertThatThrownBy(() -> downloadFileService.downloadFile(fileId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void downloadFileThrowsResourceNotFoundWhenStatusIsFailed() {
        UUID fileId = UUID.randomUUID();
        UploadedFile file = storedFile(fileId, UploadedFileStatus.FAILED);

        when(uploadedFileQueryService.getViewableUploadedFile(fileId)).thenReturn(file);

        assertThatThrownBy(() -> downloadFileService.downloadFile(fileId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void downloadFileThrowsResourceNotFoundWhenStatusIsDeleted() {
        UUID fileId = UUID.randomUUID();
        UploadedFile file = storedFile(fileId, UploadedFileStatus.DELETED);

        when(uploadedFileQueryService.getViewableUploadedFile(fileId)).thenReturn(file);

        assertThatThrownBy(() -> downloadFileService.downloadFile(fileId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void downloadFileThrowsResourceNotFoundWhenStatusIsPendingValidation() {
        UUID fileId = UUID.randomUUID();
        UploadedFile file = storedFile(fileId, UploadedFileStatus.PENDING_VALIDATION);

        when(uploadedFileQueryService.getViewableUploadedFile(fileId)).thenReturn(file);

        assertThatThrownBy(() -> downloadFileService.downloadFile(fileId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void downloadFileMarksFileAsMissingWhenStorageThrowsNotFound() {
        UUID fileId = UUID.randomUUID();
        UploadedFile file = storedFile(fileId, UploadedFileStatus.STORED);

        when(uploadedFileQueryService.getViewableUploadedFile(fileId)).thenReturn(file);
        when(fileStorageService.loadAsResource(file.getStoragePath()))
                .thenThrow(new ResourceNotFoundException("File not found"));

        assertThatThrownBy(() -> downloadFileService.downloadFile(fileId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(markFileMissingService).markFileAsMissing(fileId);
    }

    private UploadedFile storedFile(UUID fileId, UploadedFileStatus status) {
        User user = new User(
                "analyst01",
                "analyst01@example.com",
                "Analyst 01",
                null,
                false,
                UserStatus.ACTIVE
        );

        UploadedFile file = new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "/uploads/analyst01/stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                1024L,
                "checksum",
                status,
                user
        );

        return file;
    }
}