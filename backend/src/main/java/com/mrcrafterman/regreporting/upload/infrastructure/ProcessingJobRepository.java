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
            ORDER BY job.createdAt DESC
            """)
    List<ProcessingJob> findAllWithUploadedFile();

    @Query("""
            SELECT job
            FROM ProcessingJob job
            JOIN FETCH job.uploadedFile uploadedFile
            WHERE uploadedFile.uploadedBy = :username
            ORDER BY job.createdAt DESC
            """)
    List<ProcessingJob> findAllByUsername(@Param("username") String username);

    @Query("""
            SELECT job
            FROM ProcessingJob job
            JOIN FETCH job.uploadedFile uploadedFile
            WHERE job.id = :jobId
            """)
    Optional<ProcessingJob> findByIdWithUploadedFile(@Param("jobId") UUID jobId);
}
