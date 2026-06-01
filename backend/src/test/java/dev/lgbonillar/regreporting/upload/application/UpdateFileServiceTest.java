package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.processing.application.ProcessingJobCreationService;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJobStatus;
import dev.lgbonillar.regreporting.shared.BusinessConflictException;
import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRunSource;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateFileServiceTest {

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
    private UpdateFileService updateFileService;

    @Test
    void updateFileThrowsResourceNotFoundWhenFileDoesNotExist() {
        UUID fileId = UUID.randomUUID();
        User analyst = analystUser();
        MockMultipartFile newFile = new MockMultipartFile(
                "report", "updated.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{4, 5, 6}
        );

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(uploadedFileRepository.findByIdAndUploadedByIdAndStatusIn(
                fileId, analyst.getId(),
                List.of(UploadedFileStatus.STORED, UploadedFileStatus.PENDING_CORRECTION,
                        UploadedFileStatus.MISSING, UploadedFileStatus.FAILED)
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateFileService.updateFile(fileId, newFile))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateFileThrowsBusinessConflictWhenJobDoesNotAllowUpdate() {
        UUID fileId = UUID.randomUUID();
        User analyst = analystUser();
        UploadedFile existingFile = storedFile(fileId, UploadedFileStatus.STORED, analyst);
        ProcessingJob job = createProcessingJob(existingFile, ProcessingJobStatus.PROCESSING, analyst);
        MockMultipartFile newFile = new MockMultipartFile(
                "report", "updated.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{4, 5, 6}
        );

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(uploadedFileRepository.findByIdAndUploadedByIdAndStatusIn(
                fileId, analyst.getId(),
                List.of(UploadedFileStatus.STORED, UploadedFileStatus.PENDING_CORRECTION,
                        UploadedFileStatus.MISSING, UploadedFileStatus.FAILED)
        )).thenReturn(Optional.of(existingFile));
        when(processingJobCreationService.findByUploadedFile(existingFile))
                .thenReturn(Optional.of(job));
        doThrow(new BusinessConflictException("cannot be updated"))
                .when(processingJobCreationService).ensureFileCanBeUpdated(job);

        assertThatThrownBy(() -> updateFileService.updateFile(fileId, newFile))
                .isInstanceOf(BusinessConflictException.class);
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

    private UploadedFile storedFile(UUID fileId, UploadedFileStatus status, User owner) {
        return new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "/uploads/analyst01/stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                1024L,
                "checksum",
                status,
                owner
        );
    }

    private ProcessingJob createProcessingJob(UploadedFile file, ProcessingJobStatus status, User triggeredBy) {
        ProcessingJob job = new ProcessingJob(file, "Processing");
        job.startProcessing(triggeredBy);
        return job;
    }
}