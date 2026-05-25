package dev.lgbonillar.regreporting.processing.application;

import dev.lgbonillar.regreporting.processing.dto.ProcessingJobResponse;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobStatusHistoryResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProcessingJobService {

    private final ProcessingJobQueryService processingJobQueryService;
    private final ProcessingJobWorkflowService processingJobWorkflowService;
    private final ProcessingJobHistoryService processingJobHistoryService;

    public ProcessingJobService(
            ProcessingJobQueryService processingJobQueryService,
            ProcessingJobWorkflowService processingJobWorkflowService,
            ProcessingJobHistoryService processingJobHistoryService
    ) {
        this.processingJobQueryService = processingJobQueryService;
        this.processingJobWorkflowService = processingJobWorkflowService;
        this.processingJobHistoryService = processingJobHistoryService;
    }

    public List<ProcessingJobResponse> listProcessingJobs(String username) {
        return processingJobQueryService.listProcessingJobs(username);
    }

    public ProcessingJobResponse getProcessingJob(UUID jobId) {
        return processingJobQueryService.getProcessingJob(jobId);
    }

    public ProcessingJobResponse startProcessing(UUID jobId) {
        return processingJobWorkflowService.startProcessing(jobId);
    }

    public ProcessingJobResponse completeProcessing(UUID jobId) {
        return processingJobWorkflowService.completeProcessing(jobId);
    }

    public ProcessingJobResponse failProcessing(UUID jobId, String reason) {
        return processingJobWorkflowService.failProcessing(jobId, reason);
    }

    public ProcessingJobResponse approve(UUID jobId) {
        return processingJobWorkflowService.approve(jobId);
    }

    public ProcessingJobResponse reject(UUID jobId, String reason) {
        return processingJobWorkflowService.reject(jobId, reason);
    }

    public ProcessingJobResponse revoke(UUID jobId, String reason) {
        return processingJobWorkflowService.revoke(jobId, reason);
    }

    public List<ProcessingJobStatusHistoryResponse> getProcessingJobHistory(UUID jobId) {
        return processingJobHistoryService.getProcessingJobHistory(jobId);
    }

}
