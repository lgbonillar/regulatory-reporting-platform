package com.mrcrafterman.regreporting.processing.application;

import com.mrcrafterman.regreporting.shared.BusinessConflictException;
import com.mrcrafterman.regreporting.processing.domain.ProcessingJob;
import com.mrcrafterman.regreporting.processing.domain.ProcessingJobStatus;
import com.mrcrafterman.regreporting.processing.domain.ProcessingJobTransitionSource;
import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.processing.infrastructure.ProcessingJobRepository;
import com.mrcrafterman.regreporting.users.domain.User;
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
