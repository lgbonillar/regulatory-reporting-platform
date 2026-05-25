package dev.lgbonillar.regreporting.processing.application;

import dev.lgbonillar.regreporting.shared.BusinessConflictException;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJobStatus;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJobTransitionSource;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.processing.infrastructure.ProcessingJobRepository;
import dev.lgbonillar.regreporting.users.domain.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProcessingJobCreationService {

    private final ProcessingJobRepository processingJobRepository;
    private final ProcessingJobHistoryService processingJobHistoryService;

    public ProcessingJobCreationService(
            ProcessingJobRepository processingJobRepository,
            ProcessingJobHistoryService processingJobHistoryService
    ) {
        this.processingJobRepository = processingJobRepository;
        this.processingJobHistoryService = processingJobHistoryService;
    }

    public Optional<ProcessingJob> findByUploadedFile(UploadedFile uploadedFile) {
        return processingJobRepository.findByUploadedFileId(uploadedFile.getId());
    }

    public void ensureFileCanBeReplaced(ProcessingJob processingJob) {
        if (!processingJob.allowsFileModification()) {
            throw new BusinessConflictException(
                    "The file cannot be replaced because its processing job is already in state "
                            + processingJob.getStatus()
            );
        }
    }

    public void ensureFileCanBeUpdated(ProcessingJob processingJob) {
        if (!processingJob.allowsFileModification()) {
            throw new BusinessConflictException(
                    "The file cannot be updated because its processing job is already in state "
                            + processingJob.getStatus()
            );
        }
    }

    public void ensureFileCanBeDeleted(ProcessingJob processingJob) {
        if (!processingJob.allowsFileModification()) {
            throw new BusinessConflictException(
                    "The file cannot be deleted because its processing job is already in state "
                            + processingJob.getStatus()
            );
        }
    }

    public ProcessingJob createInitialJob(UploadedFile uploadedFile, User uploadedBy) {
        ProcessingJob processingJob = processingJobRepository.save(
                new ProcessingJob(uploadedFile, "File uploaded successfully")
        );

        processingJobHistoryService.recordTransition(
                processingJob,
                null,
                ProcessingJobStatus.PENDING_EXECUTION,
                ProcessingJobTransitionSource.USER,
                uploadedBy,
                "File uploaded and queued for execution"
        );

        return processingJob;
    }

    public ProcessingJob markFileReplaced(ProcessingJob processingJob) {
        processingJob.markPendingExecution("File replaced successfully");
        return processingJobRepository.save(processingJob);
    }

    public ProcessingJob markFileUpdated(ProcessingJob processingJob) {
        processingJob.markPendingExecution("File updated successfully");
        return processingJobRepository.save(processingJob);
    }
}
