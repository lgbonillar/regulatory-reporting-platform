package com.mrcrafterman.regreporting.processing.application;

import com.mrcrafterman.regreporting.processing.domain.ProcessingJob;
import com.mrcrafterman.regreporting.processing.domain.ProcessingJobStatus;
import com.mrcrafterman.regreporting.processing.domain.ProcessingJobStatusHistory;
import com.mrcrafterman.regreporting.processing.domain.ProcessingJobTransitionSource;
import com.mrcrafterman.regreporting.processing.dto.ProcessingJobStatusHistoryResponse;
import com.mrcrafterman.regreporting.processing.infrastructure.ProcessingJobStatusHistoryRepository;
import com.mrcrafterman.regreporting.users.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProcessingJobHistoryService {

    private final ProcessingJobQueryService processingJobQueryService;
    private final ProcessingJobStatusHistoryRepository processingJobStatusHistoryRepository;

    public ProcessingJobHistoryService(
            ProcessingJobQueryService processingJobQueryService,
            ProcessingJobStatusHistoryRepository processingJobStatusHistoryRepository
    ) {
        this.processingJobQueryService = processingJobQueryService;
        this.processingJobStatusHistoryRepository = processingJobStatusHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ProcessingJobStatusHistoryResponse> getProcessingJobHistory(UUID jobId) {
        ProcessingJob processingJob = processingJobQueryService.getJob(jobId);
        processingJobQueryService.requireCanView(processingJob);

        return processingJobStatusHistoryRepository
                .findByProcessingJobIdOrderByCreatedAtAsc(jobId)
                .stream()
                .map(this::toProcessingJobStatusHistoryResponse)
                .toList();
    }

    public void recordTransition(
            ProcessingJob job,
            ProcessingJobStatus previousStatus,
            ProcessingJobStatus newStatus,
            ProcessingJobTransitionSource source,
            User transitionedBy,
            String reason
    ) {
        processingJobStatusHistoryRepository.save(
                new ProcessingJobStatusHistory(
                        job,
                        previousStatus,
                        newStatus,
                        source,
                        transitionedBy,
                        reason
                )
        );
    }

    private ProcessingJobStatusHistoryResponse toProcessingJobStatusHistoryResponse(
            ProcessingJobStatusHistory history
    ) {
        return new ProcessingJobStatusHistoryResponse(
                history.getId(),
                history.getPreviousStatus() == null
                        ? null
                        : history.getPreviousStatus().name(),
                history.getNewStatus().name(),
                history.getTransitionSource().name(),
                history.getTransitionedBy() == null
                        ? null
                        : history.getTransitionedBy().getUsername(),
                history.getReason(),
                history.getCreatedAt()
        );
    }

}
