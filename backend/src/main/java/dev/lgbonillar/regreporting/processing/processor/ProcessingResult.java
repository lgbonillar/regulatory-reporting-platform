package dev.lgbonillar.regreporting.processing.processor;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;

import java.util.List;

public record ProcessingResult(
        String processorCode,
        String message,
        List<ProcessingFindingCommand> findings
) {

    public boolean hasErrors() {
        return findings.stream()
                .anyMatch(finding -> finding.severity() == ProcessingFindingSeverity.ERROR);
    }

    public static ProcessingResult successful(String processorCode, String message) {
        return new ProcessingResult(processorCode, message, List.of());
    }

    public static ProcessingResult withFindings(
            String processorCode,
            String message,
            List<ProcessingFindingCommand> findings
    ) {
        return new ProcessingResult(processorCode, message, findings);
    }
}
