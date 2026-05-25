package dev.lgbonillar.regreporting.processing.infrastructure;

import dev.lgbonillar.regreporting.processing.domain.ProcessingJobFinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProcessingJobFindingRepository extends JpaRepository<ProcessingJobFinding, UUID> {

    List<ProcessingJobFinding> findAllByProcessingJobIdOrderByCreatedAtAsc(UUID processingJobId);

    void deleteAllByProcessingJobId(UUID processingJobId);
}
