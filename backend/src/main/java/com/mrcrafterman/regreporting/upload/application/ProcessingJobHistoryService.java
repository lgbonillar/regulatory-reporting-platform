package com.mrcrafterman.regreporting.upload.application;

import com.mrcrafterman.regreporting.shared.ResourceNotFoundException;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJob;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJobStatus;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJobStatusHistory;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJobTransitionSource;
import com.mrcrafterman.regreporting.upload.dto.ProcessingJobStatusHistoryResponse;
import com.mrcrafterman.regreporting.upload.infrastructure.ProcessingJobRepository;
import com.mrcrafterman.regreporting.upload.infrastructure.ProcessingJobStatusHistoryRepository;
import com.mrcrafterman.regreporting.users.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProcessingJobHistoryService {

    private final ProcessingJobRepository processingJobRepository;
    private final ProcessingJobStatusHistoryRepository processingJobStatusHistoryRepository;

    public ProcessingJobHistoryService(
            ProcessingJobRepository processingJobRepository,
            ProcessingJobStatusHistoryRepository processingJobStatusHistoryRepository
    ) {
        this.processingJobRepository = processingJobRepository;
        this.processingJobStatusHistoryRepository = processingJobStatusHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ProcessingJobStatusHistoryResponse> getProcessingJobHistory(UUID jobId) {
        if (!processingJobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Processing job not found");
        }

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
