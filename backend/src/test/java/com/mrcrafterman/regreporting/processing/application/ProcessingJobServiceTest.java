package com.mrcrafterman.regreporting.processing.application;

import com.mrcrafterman.regreporting.processing.dto.ProcessingJobResponse;
import com.mrcrafterman.regreporting.processing.dto.ProcessingJobStatusHistoryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessingJobServiceTest {

    @Mock
    private ProcessingJobQueryService processingJobQueryService;

    @Mock
    private ProcessingJobWorkflowService processingJobWorkflowService;

    @Mock
    private ProcessingJobHistoryService processingJobHistoryService;

    @InjectMocks
    private ProcessingJobService processingJobService;

    @Test
    void listProcessingJobsDelegatesToQueryService() {
        List<ProcessingJobResponse> expected = List.of();

        when(processingJobQueryService.listProcessingJobs("analyst01")).thenReturn(expected);

        List<ProcessingJobResponse> result = processingJobService.listProcessingJobs("analyst01");

        assertThat(result).isSameAs(expected);
        verify(processingJobQueryService).listProcessingJobs("analyst01");
    }

    @Test
    void getProcessingJobDelegatesToQueryService() {
        UUID jobId = UUID.randomUUID();
        ProcessingJobResponse expected = response();

        when(processingJobQueryService.getProcessingJob(jobId)).thenReturn(expected);

        ProcessingJobResponse result = processingJobService.getProcessingJob(jobId);

        assertThat(result).isSameAs(expected);
        verify(processingJobQueryService).getProcessingJob(jobId);
    }

    @Test
    void startProcessingDelegatesToWorkflowService() {
        UUID jobId = UUID.randomUUID();
        ProcessingJobResponse expected = response();

        when(processingJobWorkflowService.startProcessing(jobId)).thenReturn(expected);

        ProcessingJobResponse result = processingJobService.startProcessing(jobId);

        assertThat(result).isSameAs(expected);
        verify(processingJobWorkflowService).startProcessing(jobId);
    }

    @Test
    void failProcessingDelegatesToWorkflowService() {
        UUID jobId = UUID.randomUUID();
        ProcessingJobResponse expected = response();

        when(processingJobWorkflowService.failProcessing(jobId, "failure reason")).thenReturn(expected);

        ProcessingJobResponse result = processingJobService.failProcessing(jobId, "failure reason");

        assertThat(result).isSameAs(expected);
        verify(processingJobWorkflowService).failProcessing(jobId, "failure reason");
    }

    @Test
    void getProcessingJobHistoryDelegatesToHistoryService() {
        UUID jobId = UUID.randomUUID();
        List<ProcessingJobStatusHistoryResponse> expected = List.of();

        when(processingJobHistoryService.getProcessingJobHistory(jobId)).thenReturn(expected);

        List<ProcessingJobStatusHistoryResponse> result =
                processingJobService.getProcessingJobHistory(jobId);

        assertThat(result).isSameAs(expected);
        verify(processingJobHistoryService).getProcessingJobHistory(jobId);
    }

    private ProcessingJobResponse response() {
        return new ProcessingJobResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "report.xlsx",
                "STORED",
                "PENDING_EXECUTION",
                "File uploaded",
                "analyst01",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

}
