package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatusHistory;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileTransitionSource;
import dev.lgbonillar.regreporting.upload.infrastructure.UploadedFileStatusHistoryRepository;
import dev.lgbonillar.regreporting.users.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UploadedFileStatusHistoryService {

    private final UploadedFileStatusHistoryRepository statusHistoryRepository;

    public UploadedFileStatusHistoryService(
            UploadedFileStatusHistoryRepository statusHistoryRepository
    ) {
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Transactional
    public UploadedFileStatusHistory recordTransition(
            UploadedFile uploadedFile,
            UploadedFileStatus previousStatus,
            UploadedFileStatus newStatus,
            UploadedFileTransitionSource transitionSource,
            User transitionedBy,
            String reason
    ) {
        return statusHistoryRepository.save(new UploadedFileStatusHistory(
                uploadedFile,
                previousStatus,
                newStatus,
                transitionSource,
                transitionedBy,
                reason
        ));
    }

    @Transactional(readOnly = true)
    public List<UploadedFileStatusHistory> listHistory(UUID uploadedFileId) {
        return
                statusHistoryRepository.findAllByUploadedFile_IdOrderByCreatedAtAsc(uploadedFileId);
    }
}
