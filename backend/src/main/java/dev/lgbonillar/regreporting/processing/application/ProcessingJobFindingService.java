package dev.lgbonillar.regreporting.processing.application;

import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJobFinding;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobFindingResponse;
import dev.lgbonillar.regreporting.processing.infrastructure.ProcessingJobFindingRepository;
import dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand;
import dev.lgbonillar.regreporting.shared.ForbiddenOperationException;
import dev.lgbonillar.regreporting.users.application.CurrentUserProvider;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProcessingJobFindingService {

    private final ProcessingJobQueryService processingJobQueryService;
    private final ProcessingJobFindingRepository processingJobFindingRepository;
    private final CurrentUserProvider currentUserProvider;

    public ProcessingJobFindingService(
            ProcessingJobQueryService processingJobQueryService,
            ProcessingJobFindingRepository processingJobFindingRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.processingJobQueryService = processingJobQueryService;
        this.processingJobFindingRepository = processingJobFindingRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public List<ProcessingJobFindingResponse> getProcessingJobFindings(UUID jobId) {
        ProcessingJob processingJob = processingJobQueryService.getJob(jobId);

        User currentUser = currentUserProvider.getCurrentUser();
        boolean isPrivileged = currentUser.hasRole(UserRole.ROOT) ||
                                currentUser.hasRole(UserRole.ADMINISTRATOR);
        boolean isOwner = currentUser.hasRole(UserRole.ANALYST) &&
                processingJob.getUploadedFile()
                        .getUploadedBy()
                        .getUsername()
                        .equals(currentUser.getUsername());

        if (!isPrivileged && !isOwner) {
            throw new ForbiddenOperationException(
                    "You are not allowed to view this processing job"
            );
        }

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
