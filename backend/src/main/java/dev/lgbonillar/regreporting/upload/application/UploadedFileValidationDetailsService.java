package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileFinding;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRun;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileFindingResponse;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileValidationRunResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UploadedFileValidationDetailsService {

    private final UploadedFileQueryService uploadedFileQueryService;
    private final UploadedFileValidationRunService validationRunService;
    private final UploadedFileFindingService findingService;
    private final UploadedFileMapper uploadedFileMapper;

    public UploadedFileValidationDetailsService(
            UploadedFileQueryService uploadedFileQueryService,
            UploadedFileValidationRunService validationRunService,
            UploadedFileFindingService findingService,
            UploadedFileMapper uploadedFileMapper
    ) {
        this.uploadedFileQueryService = uploadedFileQueryService;
        this.validationRunService = validationRunService;
        this.findingService = findingService;
        this.uploadedFileMapper = uploadedFileMapper;
    }

    public List<UploadedFileValidationRunResponse> listValidationRuns(UUID fileId) {
        UploadedFile uploadedFile = uploadedFileQueryService.getViewableUploadedFile(fileId);

        List<UploadedFileValidationRun> runs = validationRunService.listValidationRuns(
                uploadedFile.getId()
        );

        return runs.stream()
                .map(uploadedFileMapper::toValidationRunResponse)
                .toList();
    }

    public List<UploadedFileFindingResponse> listFindings(UUID fileId) {
        UploadedFile uploadedFile = uploadedFileQueryService.getViewableUploadedFile(fileId);

        List<UploadedFileFinding> findings = findingService.listFindings(uploadedFile.getId());

        return findings.stream()
                .map(uploadedFileMapper::toFindingResponse)
                .toList();
    }

    public List<UploadedFileFindingResponse> listValidationRunFindings(UUID fileId, UUID validationRunId) {
        UploadedFile uploadedFile = uploadedFileQueryService.getViewableUploadedFile(fileId);

        UploadedFileValidationRun validationRun = validationRunService.getValidationRun(
                uploadedFile.getId(),
                validationRunId
        );

        List<UploadedFileFinding> findings = findingService.listFindingsByValidationRun(
                validationRun.getId()
        );

        return findings.stream()
                .map(uploadedFileMapper::toFindingResponse)
                .toList();
    }
}