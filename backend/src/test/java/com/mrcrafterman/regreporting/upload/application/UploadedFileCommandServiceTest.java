package com.mrcrafterman.regreporting.upload.application;

import com.mrcrafterman.regreporting.processing.application.ProcessingJobCreationService;
import com.mrcrafterman.regreporting.processing.domain.ProcessingJob;
import com.mrcrafterman.regreporting.processing.domain.ProcessingJobStatus;
import com.mrcrafterman.regreporting.shared.ResourceNotFoundException;
import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.upload.domain.UploadedFileStatus;
import com.mrcrafterman.regreporting.upload.dto.ReportFileUploadResponse;
import com.mrcrafterman.regreporting.upload.dto.StoredFile;
import com.mrcrafterman.regreporting.upload.infrastructure.UploadedFileRepository;
import com.mrcrafterman.regreporting.users.application.CurrentUserProvider;
import com.mrcrafterman.regreporting.users.domain.User;
import com.mrcrafterman.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadedFileCommandServiceTest {

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @Mock
    private ProcessingJobCreationService processingJobCreationService;

    @InjectMocks
    private UploadedFileCommandService uploadedFileCommandService;

    @Test
    void uploadReportFileCreatesUploadedFileAndInitialProcessingJob() {
        User analyst = analyst();
        MockMultipartFile multipartFile = multipartFile();
        StoredFile storedFile = storedFile();

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(uploadedFileRepository.findByUploadedByIdAndOriginalFilename(
                analyst.getId(),
                "report.xlsx"
        )).thenReturn(Optional.empty());
        when(fileStorageService.store(multipartFile, "analyst01")).thenReturn(storedFile);
        when(uploadedFileRepository.save(org.mockito.ArgumentMatchers.any(UploadedFile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(processingJobCreationService.createInitialJob(
                org.mockito.ArgumentMatchers.any(UploadedFile.class),
                org.mockito.ArgumentMatchers.same(analyst)
        )).thenAnswer(invocation -> new ProcessingJob(invocation.getArgument(0), "File uploaded successfully"));

        ReportFileUploadResponse result = uploadedFileCommandService.uploadReportFile(multipartFile);

        ArgumentCaptor<UploadedFile> uploadedFileCaptor =
                ArgumentCaptor.forClass(UploadedFile.class);
        verify(uploadedFileRepository).save(uploadedFileCaptor.capture());

        UploadedFile savedFile = uploadedFileCaptor.getValue();

        assertThat(savedFile.getOriginalFilename()).isEqualTo("report.xlsx");
        assertThat(savedFile.getStoredFilename()).isEqualTo("stored-report.xlsx");
        assertThat(savedFile.getStoragePath()).isEqualTo("/uploads/stored-report.xlsx");
        assertThat(savedFile.getContentType()).isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(savedFile.getFileSize()).isEqualTo(multipartFile.getSize());
        assertThat(savedFile.getChecksum()).isEqualTo("checksum");
        assertThat(savedFile.getStatus()).isEqualTo(UploadedFileStatus.STORED);
        assertThat(savedFile.getUploadedBy()).isSameAs(analyst);

        assertThat(result.originalFilename()).isEqualTo("report.xlsx");
        assertThat(result.fileStatus()).isEqualTo(UploadedFileStatus.STORED.name());
        assertThat(result.jobStatus()).isEqualTo(ProcessingJobStatus.PENDING_EXECUTION.name());
        assertThat(result.message()).isEqualTo("File uploaded successfully");
    }

    @Test
    void markUploadedFileAsMissingThrowsResourceNotFoundWhenFileDoesNotExist() {
        UUID fileId = UUID.randomUUID();

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uploadedFileCommandService.markUploadedFileAsMissing(fileId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Uploaded file not found");
    }

    @Test
    void markUploadedFileAsMissingChangesFileStatus() {
        UUID fileId = UUID.randomUUID();
        UploadedFile uploadedFile = uploadedFile(UploadedFileStatus.STORED);

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(uploadedFile));

        uploadedFileCommandService.markUploadedFileAsMissing(fileId);

        assertThat(uploadedFile.getStatus()).isEqualTo(UploadedFileStatus.MISSING);
        assertThat(uploadedFile.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateReportFileReplacesStoredFileAndMarksJobUpdated() {
        UUID fileId = UUID.randomUUID();
        User analyst = analyst();
        UploadedFile uploadedFile = uploadedFile(UploadedFileStatus.STORED);
        ProcessingJob processingJob = new ProcessingJob(uploadedFile, "File uploaded");
        MockMultipartFile multipartFile = multipartFile();
        StoredFile storedFile = new StoredFile(
                "updated-report.xlsx",
                "/uploads/updated-report.xlsx",
                "updated-checksum"
        );

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(uploadedFileRepository.findByIdAndUploadedByIdAndStatusIn(
                fileId,
                analyst.getId(),
                java.util.List.of(
                        UploadedFileStatus.STORED,
                        UploadedFileStatus.MISSING,
                        UploadedFileStatus.FAILED
                )
        )).thenReturn(Optional.of(uploadedFile));
        when(processingJobCreationService.findByUploadedFile(uploadedFile))
                .thenReturn(Optional.of(processingJob));
        when(fileStorageService.store(multipartFile, "analyst01")).thenReturn(storedFile);
        when(processingJobCreationService.markFileUpdated(processingJob)).thenReturn(processingJob);

        ReportFileUploadResponse result = uploadedFileCommandService.updateReportFile(fileId,
                multipartFile);

        verify(processingJobCreationService).ensureFileCanBeUpdated(processingJob);
        verify(fileStorageService).delete("/uploads/stored-report.xlsx");

        assertThat(uploadedFile.getStoredFilename()).isEqualTo("updated-report.xlsx");
        assertThat(uploadedFile.getStoragePath()).isEqualTo("/uploads/updated-report.xlsx");
        assertThat(uploadedFile.getChecksum()).isEqualTo("updated-checksum");
        assertThat(uploadedFile.getStatus()).isEqualTo(UploadedFileStatus.STORED);

        assertThat(result.originalFilename()).isEqualTo("report.xlsx");
        assertThat(result.fileStatus()).isEqualTo(UploadedFileStatus.STORED.name());
        assertThat(result.jobStatus()).isEqualTo(ProcessingJobStatus.PENDING_EXECUTION.name());
    }

    @Test
    void updateReportFileThrowsResourceNotFoundWhenFileDoesNotExist() {
        UUID fileId = UUID.randomUUID();
        User analyst = analyst();
        MockMultipartFile multipartFile = multipartFile();

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(uploadedFileRepository.findByIdAndUploadedByIdAndStatusIn(
                fileId,
                analyst.getId(),
                java.util.List.of(
                        UploadedFileStatus.STORED,
                        UploadedFileStatus.MISSING,
                        UploadedFileStatus.FAILED
                )
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uploadedFileCommandService.updateReportFile(fileId, multipartFile))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Uploaded file not found");
    }

    @Test
    void deleteUploadedFileMarksFileAsDeleted() {
        UUID fileId = UUID.randomUUID();
        User analyst = analyst();
        UploadedFile uploadedFile = uploadedFile(UploadedFileStatus.STORED);
        ProcessingJob processingJob = new ProcessingJob(uploadedFile, "File uploaded");

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(uploadedFileRepository.findByIdAndUploadedByIdAndStatusIn(
                fileId,
                analyst.getId(),
                java.util.List.of(
                        UploadedFileStatus.STORED,
                        UploadedFileStatus.MISSING,
                        UploadedFileStatus.FAILED
                )
        )).thenReturn(Optional.of(uploadedFile));
        when(processingJobCreationService.findByUploadedFile(uploadedFile))
                .thenReturn(Optional.of(processingJob));

        uploadedFileCommandService.deleteUploadedFile(fileId);

        verify(processingJobCreationService).ensureFileCanBeDeleted(processingJob);
        verify(fileStorageService).delete("/uploads/stored-report.xlsx");

        assertThat(uploadedFile.getStatus()).isEqualTo(UploadedFileStatus.DELETED);
        assertThat(uploadedFile.getUpdatedAt()).isNotNull();
    }

    @Test
    void deleteUploadedFileThrowsResourceNotFoundWhenFileDoesNotExist() {
        UUID fileId = UUID.randomUUID();
        User analyst = analyst();

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(uploadedFileRepository.findByIdAndUploadedByIdAndStatusIn(
                fileId,
                analyst.getId(),
                java.util.List.of(
                        UploadedFileStatus.STORED,
                        UploadedFileStatus.MISSING,
                        UploadedFileStatus.FAILED
                )
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uploadedFileCommandService.deleteUploadedFile(fileId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Uploaded file not found");
    }

    @Test
    void uploadReportFileReplacesExistingFileAndMarksJobReplaced() {
        User analyst = analyst();
        UploadedFile existingFile = uploadedFile(UploadedFileStatus.MISSING);
        ProcessingJob existingJob = new ProcessingJob(existingFile, "File uploaded");
        MockMultipartFile multipartFile = multipartFile();
        StoredFile storedFile = new StoredFile(
                "replaced-report.xlsx",
                "/uploads/replaced-report.xlsx",
                "replaced-checksum"
        );

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(uploadedFileRepository.findByUploadedByIdAndOriginalFilename(
                analyst.getId(),
                "report.xlsx"
        )).thenReturn(Optional.of(existingFile));
        when(processingJobCreationService.findByUploadedFile(existingFile))
                .thenReturn(Optional.of(existingJob));
        when(fileStorageService.store(multipartFile, "analyst01")).thenReturn(storedFile);
        when(uploadedFileRepository.save(existingFile)).thenReturn(existingFile);
        when(processingJobCreationService.markFileReplaced(existingJob)).thenReturn(existingJob);

        ReportFileUploadResponse result = uploadedFileCommandService.uploadReportFile(multipartFile);

        verify(processingJobCreationService).ensureFileCanBeReplaced(existingJob);

        assertThat(existingFile.getStoredFilename()).isEqualTo("replaced-report.xlsx");
        assertThat(existingFile.getStoragePath()).isEqualTo("/uploads/replaced-report.xlsx");
        assertThat(existingFile.getChecksum()).isEqualTo("replaced-checksum");
        assertThat(existingFile.getStatus()).isEqualTo(UploadedFileStatus.STORED);

        assertThat(result.originalFilename()).isEqualTo("report.xlsx");
        assertThat(result.fileStatus()).isEqualTo(UploadedFileStatus.STORED.name());
        assertThat(result.jobStatus()).isEqualTo(ProcessingJobStatus.PENDING_EXECUTION.name());
    }

    @Test
    void uploadReportFileThrowsIllegalStateWhenExistingFileHasNoJob() {
        User analyst = analyst();
        UploadedFile existingFile = uploadedFile(UploadedFileStatus.STORED);
        MockMultipartFile multipartFile = multipartFile();

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(uploadedFileRepository.findByUploadedByIdAndOriginalFilename(
                analyst.getId(),
                "report.xlsx"
        )).thenReturn(Optional.of(existingFile));
        when(processingJobCreationService.findByUploadedFile(existingFile))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> uploadedFileCommandService.uploadReportFile(multipartFile))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Processing job not found for uploaded file");
    }

    @Test
    void updateReportFileDoesNotDeletePreviousStorageWhenPathDidNotChange() {
        UUID fileId = UUID.randomUUID();
        User analyst = analyst();
        UploadedFile uploadedFile = uploadedFile(UploadedFileStatus.STORED);
        ProcessingJob processingJob = new ProcessingJob(uploadedFile, "File uploaded");
        MockMultipartFile multipartFile = multipartFile();
        StoredFile storedFile = new StoredFile(
                "stored-report.xlsx",
                "/uploads/stored-report.xlsx",
                "updated-checksum"
        );

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(uploadedFileRepository.findByIdAndUploadedByIdAndStatusIn(
                fileId,
                analyst.getId(),
                List.of(
                        UploadedFileStatus.STORED,
                        UploadedFileStatus.MISSING,
                        UploadedFileStatus.FAILED
                )
        )).thenReturn(Optional.of(uploadedFile));
        when(processingJobCreationService.findByUploadedFile(uploadedFile))
                .thenReturn(Optional.of(processingJob));
        when(fileStorageService.store(multipartFile, "analyst01")).thenReturn(storedFile);
        when(processingJobCreationService.markFileUpdated(processingJob)).thenReturn(processingJob);

        uploadedFileCommandService.updateReportFile(fileId, multipartFile);

        verify(fileStorageService, never()).delete("/uploads/stored-report.xlsx");

        assertThat(uploadedFile.getChecksum()).isEqualTo("updated-checksum");
    }

    @Test
    void updateReportFileThrowsIllegalStateWhenFileHasNoJob() {
        UUID fileId = UUID.randomUUID();
        User analyst = analyst();
        UploadedFile uploadedFile = uploadedFile(UploadedFileStatus.STORED);
        MockMultipartFile multipartFile = multipartFile();

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(uploadedFileRepository.findByIdAndUploadedByIdAndStatusIn(
                fileId,
                analyst.getId(),
                List.of(
                        UploadedFileStatus.STORED,
                        UploadedFileStatus.MISSING,
                        UploadedFileStatus.FAILED
                )
        )).thenReturn(Optional.of(uploadedFile));
        when(processingJobCreationService.findByUploadedFile(uploadedFile))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> uploadedFileCommandService.updateReportFile(fileId, multipartFile))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Processing job not found for uploaded file");
    }

    @Test
    void deleteUploadedFileThrowsIllegalStateWhenFileHasNoJob() {
        UUID fileId = UUID.randomUUID();
        User analyst = analyst();
        UploadedFile uploadedFile = uploadedFile(UploadedFileStatus.STORED);

        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(uploadedFileRepository.findByIdAndUploadedByIdAndStatusIn(
                fileId,
                analyst.getId(),
                List.of(
                        UploadedFileStatus.STORED,
                        UploadedFileStatus.MISSING,
                        UploadedFileStatus.FAILED
                )
        )).thenReturn(Optional.of(uploadedFile));
        when(processingJobCreationService.findByUploadedFile(uploadedFile))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> uploadedFileCommandService.deleteUploadedFile(fileId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Processing job not found for uploaded file");
    }

    private MockMultipartFile multipartFile() {
        return new MockMultipartFile(
                "file",
                "report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "content".getBytes()
        );
    }

    private StoredFile storedFile() {
        return new StoredFile(
                "stored-report.xlsx",
                "/uploads/stored-report.xlsx",
                "checksum"
        );
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
                analyst()
        );
    }

    private User analyst() {
        User user = new User(
                "analyst01",
                "analyst01@example.com",
                "Analyst 01",
                null,
                false,
                UserStatus.ACTIVE
        );
        user.setId(UUID.randomUUID());
        return user;
    }

}
