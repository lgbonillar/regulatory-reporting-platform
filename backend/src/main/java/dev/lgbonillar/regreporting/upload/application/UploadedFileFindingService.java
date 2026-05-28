package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileFinding;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRun;
import dev.lgbonillar.regreporting.upload.infrastructure.UploadedFileFindingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UploadedFileFindingService {

    private final UploadedFileFindingRepository findingRepository;

    public UploadedFileFindingService(UploadedFileFindingRepository findingRepository) {
        this.findingRepository = findingRepository;
    }

    @Transactional
    public List<UploadedFileFinding> saveFindings(
            UploadedFileValidationRun validationRun,
            UploadedFile uploadedFile,
            List<ProcessingFindingCommand> findings
    ) {
        List<UploadedFileFinding> entities = findings.stream()
                .map(finding -> new UploadedFileFinding(
                        validationRun,
                        uploadedFile,
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

        return findingRepository.saveAll(entities);
    }

    @Transactional(readOnly = true)
    public List<UploadedFileFinding> listFindings(UUID uploadedFileId) {
        return findingRepository.findAllByUploadedFile_IdOrderByCreatedAtAsc(uploadedFileId);
    }

    @Transactional(readOnly = true)
    public List<UploadedFileFinding> listFindingsByValidationRun(UUID validationRunId) {
        return findingRepository.findAllByValidationRun_IdOrderByCreatedAtAsc(validationRunId);
    }
}
