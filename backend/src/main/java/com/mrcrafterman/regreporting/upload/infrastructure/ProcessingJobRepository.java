package com.mrcrafterman.regreporting.upload.infrastructure;

import com.mrcrafterman.regreporting.upload.domain.ProcessingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessingJobRepository extends JpaRepository<ProcessingJob, UUID> {

    Optional<ProcessingJob> findByUploadedFileId(UUID uploadedFileId);

    @Query("""
          SELECT job
          FROM ProcessingJob job
          JOIN FETCH job.uploadedFile uploadedFile
          JOIN FETCH uploadedFile.uploadedBy uploadedBy
          LEFT JOIN FETCH job.triggeredBy triggeredBy
          LEFT JOIN FETCH job.approvedBy approvedBy
          LEFT JOIN FETCH job.rejectedBy rejectedBy
          LEFT JOIN FETCH job.revokedBy revokedBy
          ORDER BY job.createdAt DESC
          """)
    List<ProcessingJob> findAllWithUploadedFile();

    @Query("""
          SELECT job
          FROM ProcessingJob job
          JOIN FETCH job.uploadedFile uploadedFile
          JOIN FETCH uploadedFile.uploadedBy uploadedBy
          LEFT JOIN FETCH job.triggeredBy triggeredBy
          LEFT JOIN FETCH job.approvedBy approvedBy
          LEFT JOIN FETCH job.rejectedBy rejectedBy
          LEFT JOIN FETCH job.revokedBy revokedBy
          WHERE uploadedBy.username = :username
          ORDER BY job.createdAt DESC
          """)
    List<ProcessingJob> findAllByUsername(@Param("username") String username);

    @Query("""
          SELECT job
          FROM ProcessingJob job
          JOIN FETCH job.uploadedFile uploadedFile
          JOIN FETCH uploadedFile.uploadedBy uploadedBy
          LEFT JOIN FETCH job.triggeredBy triggeredBy
          LEFT JOIN FETCH job.approvedBy approvedBy
          LEFT JOIN FETCH job.rejectedBy rejectedBy
          LEFT JOIN FETCH job.revokedBy revokedBy
          WHERE job.id = :jobId
          """)
    Optional<ProcessingJob> findByIdWithUploadedFile(@Param("jobId") UUID jobId);

}
