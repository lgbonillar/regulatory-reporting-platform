package dev.lgbonillar.regreporting.upload.validation;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand;

import java.util.List;

public record UploadedFileValidationResult(
        List<ProcessingFindingCommand> findings
) {

    public boolean hasErrors() {
        return findings.stream()
                .anyMatch(finding -> finding.severity() == ProcessingFindingSeverity.ERROR);
    }

    public static UploadedFileValidationResult passed() {
        return new UploadedFileValidationResult(List.of());
    }

    public static UploadedFileValidationResult withFindings(
            List<ProcessingFindingCommand> findings
    ) {
        return new UploadedFileValidationResult(findings);
    }
}
