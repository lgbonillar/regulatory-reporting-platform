package dev.lgbonillar.regreporting.processing.application;

import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJobFinding;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobFindingResponse;
import dev.lgbonillar.regreporting.processing.infrastructure.ProcessingJobFindingRepository;
import dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand;
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

    @Transactional
    public void replaceProcessingJobFindings(
            ProcessingJob processingJob,
            List<ProcessingFindingCommand> findings
    ) {
        processingJobFindingRepository.deleteAllByProcessingJobId(processingJob.getId());

        List<ProcessingJobFinding> entities = findings.stream()
                .map(finding -> new ProcessingJobFinding(
                        processingJob,
                        finding.severity(),
                        finding.scope(),
                        finding.code(),
                        finding.message(),
                        finding.sheetName(),
                        finding.rowNumber(),
                        finding.columnName(),
                        finding.fieldName(),
                        finding.rejectedValue(),
                        finding.expectedValue(),
                        finding.actualValue()
                ))
                .toList();

        processingJobFindingRepository.saveAll(entities);
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
