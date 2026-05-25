package dev.lgbonillar.regreporting.processing.application;

import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJobFinding;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobFindingResponse;
import dev.lgbonillar.regreporting.processing.infrastructure.ProcessingJobFindingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProcessingJobFindingService {

    private final ProcessingJobQueryService processingJobQueryService;
    private final ProcessingJobFindingRepository processingJobFindingRepository;

    public ProcessingJobFindingService(
            ProcessingJobQueryService processingJobQueryService,
            ProcessingJobFindingRepository processingJobFindingRepository
    ) {
        this.processingJobQueryService = processingJobQueryService;
        this.processingJobFindingRepository = processingJobFindingRepository;
    }

    @Transactional(readOnly = true)
    public List<ProcessingJobFindingResponse> getProcessingJobFindings(UUID jobId) {
        ProcessingJob processingJob = processingJobQueryService.getJob(jobId);
        processingJobQueryService.requireCanView(processingJob);

        return processingJobFindingRepository.findAllByProcessingJobIdOrderByCreatedAtAsc(jobId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ProcessingJobFindingResponse toResponse(ProcessingJobFinding finding) {
        return new ProcessingJobFindingResponse(
                finding.getId(),
                finding.getSeverity().name(),
                finding.getScope().name(),
                finding.getCode(),
                finding.getMessage(),
                finding.getSheetName(),
                finding.getRowNumber(),
                finding.getColumnName(),
                finding.getFieldName(),
                finding.getRejectedValue(),
                finding.getExpectedValue(),
                finding.getActualValue(),
                finding.getCreatedAt()
        );
    }

}
