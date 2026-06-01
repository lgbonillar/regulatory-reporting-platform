package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.processing.application.ProcessingJobCreationService;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJobStatus;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.dto.StoredFile;
import dev.lgbonillar.regreporting.upload.infrastructure.UploadedFileRepository;
import dev.lgbonillar.regreporting.users.application.CurrentUserProvider;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadFileServiceTest {

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @Mock
    private ProcessingJobCreationService processingJobCreationService;

    @Mock
    private UploadedFileStatusHistoryService uploadedFileStatusHistoryService;

    @Mock
    private UploadedFileValidationService uploadedFileValidationService;

    @InjectMocks
    private UploadFileService uploadFileService;

    @Test
    void uploadFileThrowsNullPointerExceptionWhenFileNameIsNull() {
        User analyst = analystUser();
        MockMultipartFile file = new MockMultipartFile(
                "report", null,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{1, 2, 3}
        );
        StoredFile storedFile = new StoredFile(
                "anything.xlsx",
                "stored-anything.xlsx",
                "/uploads/analyst01/stored-anything.xlsx",
                "checksum"
        );
        UploadedFile savedFile = createSavedFile(analyst);

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(fileStorageService.store(any(), any())).thenReturn(storedFile);
        when(uploadedFileRepository.save(any())).thenReturn(savedFile);
        when(uploadedFileValidationService.validate(any(), any(), any())).thenReturn(null);

        assertThatThrownBy(() -> uploadFileService.uploadFile(file))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void uploadFileStoresFileWithCorrectProperties() {
        User analyst = analystUser();
        MockMultipartFile file = new MockMultipartFile(
                "report", "report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{1, 2, 3}
        );
        StoredFile storedFile = new StoredFile(
                "report.xlsx",
                "stored-report.xlsx",
                "/uploads/analyst01/stored-report.xlsx",
                "checksum123"
        );
        UploadedFile savedFile = createSavedFile(analyst);

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(fileStorageService.store(file, analyst)).thenReturn(storedFile);
        when(uploadedFileRepository.save(any(UploadedFile.class))).thenReturn(savedFile);
        when(uploadedFileValidationService.validate(any(), any(), any()))
                .thenReturn(UploadedFileStatus.PENDING_CORRECTION);

        uploadFileService.uploadFile(file);

        verify(fileStorageService).store(file, analyst);
    }

    private User analystUser() {
        return new User(
                "analyst01",
                "analyst01@example.com",
                "Analyst 01",
                null,
                false,
                UserStatus.ACTIVE
        );
    }

    private UploadedFile createSavedFile(User owner) {
        return new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "/uploads/analyst01/stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                1024L,
                "checksum123",
                UploadedFileStatus.PENDING_VALIDATION,
                owner
        );
    }

    private ProcessingJob createProcessingJob(UploadedFile file, ProcessingJobStatus status, User triggeredBy) {
        ProcessingJob job = new ProcessingJob(file, "Processing");
        job.startProcessing(triggeredBy);
        return job;
    }
}