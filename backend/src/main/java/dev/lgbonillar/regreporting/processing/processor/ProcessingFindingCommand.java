package dev.lgbonillar.regreporting.processing.processor;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;

public record ProcessingFindingCommand(
        ProcessingFindingSeverity severity,
        ProcessingFindingScope scope,
        String code,
        String message,
        String sheetName,
        Integer rowNumber,
        String columnName,
        String fieldName,
        String rejectedValue,
        String expectedValue,
        String actualValue
) {
}