package com.mrcrafterman.regreporting.processing.application;

import com.mrcrafterman.regreporting.processing.domain.ProcessingJob;
import com.mrcrafterman.regreporting.processing.domain.ProcessingJobStatus;
import com.mrcrafterman.regreporting.processing.domain.ProcessingJobTransitionSource;
import com.mrcrafterman.regreporting.processing.infrastructure.ProcessingJobRepository;
import com.mrcrafterman.regreporting.shared.BusinessConflictException;
import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.upload.domain.UploadedFileStatus;
import com.mrcrafterman.regreporting.users.domain.User;
import com.mrcrafterman.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessingJobCreationServiceTest {

    @Mock
    private ProcessingJobRepository processingJobRepository;

    @Mock
    private ProcessingJobHistoryService processingJobHistoryService;

    @InjectMocks
    private ProcessingJobCreationService processingJobCreationService;

    @Test
    void findByUploadedFileSearchesByUploadedFileId() {
        UploadedFile uploadedFile = storedFile();
        ProcessingJob processingJob = new ProcessingJob(uploadedFile, "File uploaded");

        when(processingJobRepository.findByUploadedFileId(uploadedFile.getId()))
                .thenReturn(Optional.of(processingJob));

        Optional<ProcessingJob> result = processingJobCreationService.findByUploadedFile(uploadedFile);

        assertThat(result).containsSame(processingJob);
    }

    @Test
    void createInitialJobSavesPendingJobAndRecordsHistory() {
        UploadedFile uploadedFile = storedFile();
        User analyst = analyst();

        when(processingJobRepository.save(org.mockito.ArgumentMatchers.any(ProcessingJob.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProcessingJob result = processingJobCreationService.createInitialJob(uploadedFile, analyst);

        ArgumentCaptor<ProcessingJob> jobCaptor = ArgumentCaptor.forClass(ProcessingJob.class);
        verify(processingJobRepository).save(jobCaptor.capture());

        ProcessingJob savedJob = jobCaptor.getValue();

        assertThat(result).isSameAs(savedJob);
        assertThat(savedJob.getUploadedFile()).isSameAs(uploadedFile);
        assertThat(savedJob.getStatus()).isEqualTo(ProcessingJobStatus.PENDING_EXECUTION);
        assertThat(savedJob.getMessage()).isEqualTo("File uploaded successfully");

        verify(processingJobHistoryService).recordTransition(
                savedJob,
                null,
                ProcessingJobStatus.PENDING_EXECUTION,
                ProcessingJobTransitionSource.USER,
                analyst,
                "File uploaded and queued for execution"
        );
    }

    @Test
    void ensureFileCanBeReplacedAllowsPendingJob() {
        ProcessingJob job = pendingJob();

        processingJobCreationService.ensureFileCanBeReplaced(job);
    }

    @Test
    void ensureFileCanBeReplacedThrowsWhenJobAlreadyStarted() {
        ProcessingJob job = processingJob();

        assertThatThrownBy(() -> processingJobCreationService.ensureFileCanBeReplaced(job))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining(ProcessingJobStatus.PROCESSING.name());
    }

    @Test
    void ensureFileCanBeUpdatedThrowsWhenJobAlreadyStarted() {
        ProcessingJob job = processingJob();

        assertThatThrownBy(() -> processingJobCreationService.ensureFileCanBeUpdated(job))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining(ProcessingJobStatus.PROCESSING.name());
    }

    @Test
    void ensureFileCanBeDeletedThrowsWhenJobAlreadyStarted() {
        ProcessingJob job = processingJob();

        assertThatThrownBy(() -> processingJobCreationService.ensureFileCanBeDeleted(job))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining(ProcessingJobStatus.PROCESSING.name());
    }

    @Test
    void markFileReplacedUpdatesPendingMessageAndSavesJob() {
        ProcessingJob job = pendingJob();

        when(processingJobRepository.save(job)).thenReturn(job);

        ProcessingJob result = processingJobCreationService.markFileReplaced(job);

        assertThat(result).isSameAs(job);
        assertThat(job.getMessage()).isEqualTo("File replaced successfully");
        assertThat(job.getUpdatedAt()).isNotNull();

        verify(processingJobRepository).save(job);
    }

    @Test
    void markFileUpdatedUpdatesPendingMessageAndSavesJob() {
        ProcessingJob job = pendingJob();

        when(processingJobRepository.save(job)).thenReturn(job);

        ProcessingJob result = processingJobCreationService.markFileUpdated(job);

        assertThat(result).isSameAs(job);
        assertThat(job.getMessage()).isEqualTo("File updated successfully");
        assertThat(job.getUpdatedAt()).isNotNull();

        verify(processingJobRepository).save(job);
    }

    private ProcessingJob pendingJob() {
        return new ProcessingJob(storedFile(), "File uploaded");
    }

    private ProcessingJob processingJob() {
        ProcessingJob job = pendingJob();
        job.startProcessing(administrator());
        return job;
    }

    private UploadedFile storedFile() {
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
        uploadedFile.setId(UUID.randomUUID());
        return uploadedFile;
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

    private User administrator() {
        return new User(
                "admin01",
                "admin01@example.com",
                "Admin 01",
                null,
                false,
                UserStatus.ACTIVE
        );
    }

}
