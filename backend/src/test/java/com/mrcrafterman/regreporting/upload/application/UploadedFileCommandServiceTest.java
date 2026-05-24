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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

                ReportFileUploadResponse result =
                uploadedFileCommandService.uploadReportFile(multipartFile);

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
