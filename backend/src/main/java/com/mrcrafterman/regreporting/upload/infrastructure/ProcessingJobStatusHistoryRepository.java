package com.mrcrafterman.regreporting.upload.infrastructure;

import com.mrcrafterman.regreporting.upload.domain.ProcessingJobStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProcessingJobStatusHistoryRepository
        extends JpaRepository<ProcessingJobStatusHistory, UUID> {

    List<ProcessingJobStatusHistory> findByProcessingJobIdOrderByCreatedAtAsc(UUID processingJobId);
}
