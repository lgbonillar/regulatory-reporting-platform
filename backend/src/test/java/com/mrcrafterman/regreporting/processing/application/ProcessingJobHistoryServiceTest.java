package com.mrcrafterman.regreporting.processing.application;

import com.mrcrafterman.regreporting.processing.domain.ProcessingJob;
import com.mrcrafterman.regreporting.processing.domain.ProcessingJobStatus;
import com.mrcrafterman.regreporting.processing.domain.ProcessingJobStatusHistory;
import com.mrcrafterman.regreporting.processing.domain.ProcessingJobTransitionSource;
import com.mrcrafterman.regreporting.processing.dto.ProcessingJobStatusHistoryResponse;
import com.mrcrafterman.regreporting.processing.infrastructure.ProcessingJobRepository;
import com.mrcrafterman.regreporting.processing.infrastructure.ProcessingJobStatusHistoryRepository;
import com.mrcrafterman.regreporting.shared.ResourceNotFoundException;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessingJobHistoryServiceTest {

    @Mock
    private ProcessingJobRepository processingJobRepository;

    @Mock
    private ProcessingJobStatusHistoryRepository processingJobStatusHistoryRepository;

    @InjectMocks
    private ProcessingJobHistoryService processingJobHistoryService;

    @Test
    void getProcessingJobHistoryThrowsResourceNotFoundWhenJobDoesNotExist() {
        UUID jobId = UUID.randomUUID();

        when(processingJobRepository.existsById(jobId)).thenReturn(false);

        assertThatThrownBy(() -> processingJobHistoryService.getProcessingJobHistory(jobId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Processing job not found");

        verifyNoInteractions(processingJobStatusHistoryRepository);
    }

    @Test
    void getProcessingJobHistoryReturnsMappedHistory() {
        UUID jobId = UUID.randomUUID();
        ProcessingJob job = pendingJob();
        User admin = administrator();

        ProcessingJobStatusHistory started = new ProcessingJobStatusHistory(
                job,
                ProcessingJobStatus.PENDING_EXECUTION,
                ProcessingJobStatus.PROCESSING,
                ProcessingJobTransitionSource.USER,
                admin,
                "Administrator started processing"
        );

        ProcessingJobStatusHistory completed = new ProcessingJobStatusHistory(
                job,
                ProcessingJobStatus.PROCESSING,
                ProcessingJobStatus.AWAITING_APPROVAL,
                ProcessingJobTransitionSource.SYSTEM,
                null,
                "Automatic processing completed successfully"
        );

        when(processingJobRepository.existsById(jobId)).thenReturn(true);
        when(processingJobStatusHistoryRepository.findByProcessingJobIdOrderByCreatedAtAsc(jobId))
                .thenReturn(List.of(started, completed));

        List<ProcessingJobStatusHistoryResponse> result =
                processingJobHistoryService.getProcessingJobHistory(jobId);

        assertThat(result).hasSize(2);


        assertThat(result.getFirst().previousStatus()).isEqualTo(ProcessingJobStatus.PENDING_EXECUTION.name());

        assertThat(result.getFirst().newStatus()).isEqualTo(ProcessingJobStatus.PROCESSING.name());

        assertThat(result.getFirst().transitionSource()).isEqualTo(ProcessingJobTransitionSource.USER.name());
        assertThat(result.getFirst().transitionedBy()).isEqualTo("admin01");


        assertThat(result.getLast().previousStatus()).isEqualTo(ProcessingJobStatus.PROCESSING.name());

        assertThat(result.getLast().newStatus()).isEqualTo(ProcessingJobStatus.AWAITING_APPROVAL.name());

        assertThat(result.getLast().transitionSource()).isEqualTo(ProcessingJobTransitionSource.SYSTEM.name());
        assertThat(result.getLast().transitionedBy()).isNull();
    }

    @Test
    void recordTransitionSavesHistoryEntry() {
        ProcessingJob job = pendingJob();
        User admin = administrator();

        processingJobHistoryService.recordTransition(
                job,
                ProcessingJobStatus.PENDING_EXECUTION,
                ProcessingJobStatus.PROCESSING,
                ProcessingJobTransitionSource.USER,
                admin,
                "Administrator started processing"
        );

        ArgumentCaptor<ProcessingJobStatusHistory> captor =
                ArgumentCaptor.forClass(ProcessingJobStatusHistory.class);

        verify(processingJobStatusHistoryRepository).save(captor.capture());

        ProcessingJobStatusHistory saved = captor.getValue();

        assertThat(saved.getProcessingJob()).isSameAs(job);
        assertThat(saved.getPreviousStatus()).isEqualTo(ProcessingJobStatus.PENDING_EXECUTION);
        assertThat(saved.getNewStatus()).isEqualTo(ProcessingJobStatus.PROCESSING);
        assertThat(saved.getTransitionSource()).isEqualTo(ProcessingJobTransitionSource.USER);
        assertThat(saved.getTransitionedBy()).isSameAs(admin);
        assertThat(saved.getReason()).isEqualTo("Administrator started processing");
    }

    private ProcessingJob pendingJob() {
        return new ProcessingJob(storedFile(), "File uploaded");
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
