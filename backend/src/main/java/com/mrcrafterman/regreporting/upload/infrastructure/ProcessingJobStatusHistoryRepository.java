package com.mrcrafterman.regreporting.upload.infrastructure;

import com.mrcrafterman.regreporting.upload.domain.ProcessingJobStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProcessingJobStatusHistoryRepository
        extends JpaRepository<ProcessingJobStatusHistory, UUID> {

    @Query("""
            SELECT history
            FROM ProcessingJobStatusHistory history
            LEFT JOIN FETCH history.transitionedBy
            WHERE history.processingJob.id = :processingJobId
            ORDER BY history.createdAt ASC
            """)
    List<ProcessingJobStatusHistory> findByProcessingJobIdOrderByCreatedAtAsc(
            @Param("processingJobId") UUID processingJobId
    );

}
