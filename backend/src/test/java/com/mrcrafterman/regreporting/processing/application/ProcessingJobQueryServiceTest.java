package com.mrcrafterman.regreporting.processing.application;

import com.mrcrafterman.regreporting.processing.domain.ProcessingJob;
import com.mrcrafterman.regreporting.processing.domain.ProcessingJobStatus;
import com.mrcrafterman.regreporting.processing.dto.ProcessingJobResponse;
import com.mrcrafterman.regreporting.processing.infrastructure.ProcessingJobRepository;
import com.mrcrafterman.regreporting.shared.ResourceNotFoundException;
import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.upload.domain.UploadedFileStatus;
import com.mrcrafterman.regreporting.users.domain.User;
import com.mrcrafterman.regreporting.users.domain.UserStatus;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessingJobQueryServiceTest {

    @Mock
    private ProcessingJobRepository processingJobRepository;

    @InjectMocks
    private ProcessingJobQueryService processingJobQueryService;

    @Test
    void listProcessingJobsWithoutUsernameFindsAllJobs() {
        ProcessingJob job = pendingJob();

        when(processingJobRepository.findAllWithUploadedFile()).thenReturn(List.of(job));

        List<ProcessingJobResponse> result = processingJobQueryService.listProcessingJobs(null);

        assertThat(result).hasSize(1);

        assertThat(result.getFirst().jobStatus()).isEqualTo(ProcessingJobStatus.PENDING_EXECUTION.name());

        verify(processingJobRepository).findAllWithUploadedFile();
    }

    @Test
    void listProcessingJobsWithBlankUsernameFindsAllJobs() {
        ProcessingJob job = pendingJob();

        when(processingJobRepository.findAllWithUploadedFile()).thenReturn(List.of(job));

        List<ProcessingJobResponse> result = processingJobQueryService.listProcessingJobs(" ");

        assertThat(result).hasSize(1);

        verify(processingJobRepository).findAllWithUploadedFile();
    }

    @Test
    void listProcessingJobsWithUsernameFiltersByUsername() {
        ProcessingJob job = pendingJob();

        when(processingJobRepository.findAllByUsername("analyst01")).thenReturn(List.of(job));

        List<ProcessingJobResponse> result =
                processingJobQueryService.listProcessingJobs("analyst01");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().uploadedBy()).isEqualTo("analyst01");

        verify(processingJobRepository).findAllByUsername("analyst01");
    }

    @Test
    void getProcessingJobReturnsMappedResponse() {
        UUID jobId = UUID.randomUUID();
        ProcessingJob job = awaitingApprovalJob();


        when(processingJobRepository.findByIdWithUploadedFile(jobId)).thenReturn(Optional.of(job));

        ProcessingJobResponse result = processingJobQueryService.getProcessingJob(jobId);

        assertThat(result.originalFilename()).isEqualTo("report.xlsx");
        assertThat(result.fileStatus()).isEqualTo(UploadedFileStatus.STORED.name());
        assertThat(result.jobStatus()).isEqualTo(ProcessingJobStatus.AWAITING_APPROVAL.name());
        assertThat(result.uploadedBy()).isEqualTo("analyst01");
        assertThat(result.triggeredBy()).isEqualTo("admin01");
        assertThat(result.processingCompletedAt()).isNotNull();
    }

    @Test
    void getJobThrowsResourceNotFoundWhenJobDoesNotExist() {
        UUID jobId = UUID.randomUUID();


        when(processingJobRepository.findByIdWithUploadedFile(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processingJobQueryService.getJob(jobId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Processing job not found");
    }

    private ProcessingJob pendingJob() {
        return new ProcessingJob(storedFile(), "File uploaded");
    }

    private ProcessingJob awaitingApprovalJob() {
        ProcessingJob job = pendingJob();
        job.startProcessing(administrator());
        job.markProcessingCompleted();
        return job;
    }

    private UploadedFile storedFile() {
        return new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "/uploads/stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                1024L,
                "checksum",
                UploadedFileStatus.STORED,
                analyst()
        );
    }

    private User analyst() {
        return new User("analyst01", "analyst01@example.com", "Analyst 01", null, false,
                UserStatus.ACTIVE);
    }

    private User administrator() {
        return new User("admin01", "admin01@example.com", "Admin 01", null, false,
                UserStatus.ACTIVE);
    }

}
