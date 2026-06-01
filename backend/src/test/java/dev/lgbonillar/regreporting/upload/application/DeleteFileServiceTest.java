package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.processing.application.ProcessingJobCreationService;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJobStatus;
import dev.lgbonillar.regreporting.shared.BusinessConflictException;
import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileTransitionSource;
import dev.lgbonillar.regreporting.upload.infrastructure.UploadedFileRepository;
import dev.lgbonillar.regreporting.users.application.CurrentUserProvider;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteFileServiceTest {

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

    @InjectMocks
    private DeleteFileService deleteFileService;

    @Test
    void deleteFileMarksFileAsDeletedWhenAllowed() {
        UUID fileId = UUID.randomUUID();
        User analyst = analystUser();
        UploadedFile file = storedFile(fileId, UploadedFileStatus.STORED, analyst);

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(uploadedFileRepository.findByIdAndUploadedByIdAndStatusIn(
                fileId,
                analyst.getId(),
                List.of(UploadedFileStatus.STORED, UploadedFileStatus.PENDING_CORRECTION,
                        UploadedFileStatus.MISSING, UploadedFileStatus.FAILED)
        )).thenReturn(Optional.of(file));
        when(processingJobCreationService.findByUploadedFile(file)).thenReturn(Optional.empty());
        doNothing().when(fileStorageService).delete(file.getStoragePath());
        when(uploadedFileStatusHistoryService.recordTransition(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(null);

        deleteFileService.deleteFile(fileId);

        verify(fileStorageService).delete(file.getStoragePath());
        assertThat(file.getStatus()).isEqualTo(UploadedFileStatus.DELETED);
    }

    @Test
    void deleteFileThrowsResourceNotFoundWhenFileDoesNotExist() {
        UUID fileId = UUID.randomUUID();
        User analyst = analystUser();

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(uploadedFileRepository.findByIdAndUploadedByIdAndStatusIn(
                fileId,
                analyst.getId(),
                List.of(UploadedFileStatus.STORED, UploadedFileStatus.PENDING_CORRECTION,
                        UploadedFileStatus.MISSING, UploadedFileStatus.FAILED)
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteFileService.deleteFile(fileId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteFileThrowsBusinessConflictWhenJobDoesNotAllowModification() {
        UUID fileId = UUID.randomUUID();
        User analyst = analystUser();
        UploadedFile file = storedFile(fileId, UploadedFileStatus.STORED, analyst);
        ProcessingJob job = createProcessingJob(file, ProcessingJobStatus.PROCESSING, analyst);

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(uploadedFileRepository.findByIdAndUploadedByIdAndStatusIn(
                fileId,
                analyst.getId(),
                List.of(UploadedFileStatus.STORED, UploadedFileStatus.PENDING_CORRECTION,
                        UploadedFileStatus.MISSING, UploadedFileStatus.FAILED)
        )).thenReturn(Optional.of(file));
        when(processingJobCreationService.findByUploadedFile(file)).thenReturn(Optional.of(job));
        doThrow(new dev.lgbonillar.regreporting.shared.BusinessConflictException("File cannot be deleted in PROCESSING state"))
                .when(processingJobCreationService).ensureFileCanBeDeleted(job);

        assertThatThrownBy(() -> deleteFileService.deleteFile(fileId))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("cannot be deleted");
    }

    @Test
    void deleteFileDeletesPhysicalFileAndMarksStatusDeleted() {
        UUID fileId = UUID.randomUUID();
        User analyst = analystUser();
        UploadedFile file = storedFile(fileId, UploadedFileStatus.PENDING_CORRECTION, analyst);

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(uploadedFileRepository.findByIdAndUploadedByIdAndStatusIn(
                fileId,
                analyst.getId(),
                List.of(UploadedFileStatus.STORED, UploadedFileStatus.PENDING_CORRECTION,
                        UploadedFileStatus.MISSING, UploadedFileStatus.FAILED)
        )).thenReturn(Optional.of(file));
        when(processingJobCreationService.findByUploadedFile(file)).thenReturn(Optional.empty());
        doNothing().when(fileStorageService).delete(file.getStoragePath());
        when(uploadedFileStatusHistoryService.recordTransition(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(null);

        deleteFileService.deleteFile(fileId);

        verify(fileStorageService).delete(file.getStoragePath());
        assertThat(file.getStatus()).isEqualTo(UploadedFileStatus.DELETED);
    }

    @Test
    void deleteFileRecordsHistoryTransition() {
        UUID fileId = UUID.randomUUID();
        User analyst = analystUser();
        UploadedFile file = storedFile(fileId, UploadedFileStatus.MISSING, analyst);

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(uploadedFileRepository.findByIdAndUploadedByIdAndStatusIn(
                fileId,
                analyst.getId(),
                List.of(UploadedFileStatus.STORED, UploadedFileStatus.PENDING_CORRECTION,
                        UploadedFileStatus.MISSING, UploadedFileStatus.FAILED)
        )).thenReturn(Optional.of(file));
        when(processingJobCreationService.findByUploadedFile(file)).thenReturn(Optional.empty());
        doNothing().when(fileStorageService).delete(file.getStoragePath());
        when(uploadedFileStatusHistoryService.recordTransition(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(null);

        deleteFileService.deleteFile(fileId);

        verify(uploadedFileStatusHistoryService).recordTransition(
                file,
                UploadedFileStatus.MISSING,
                UploadedFileStatus.DELETED,
                UploadedFileTransitionSource.USER,
                analyst,
                "File deleted"
        );
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