package dev.lgbonillar.regreporting.processing.processor;

import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;

public interface RegulatoryReportProcessor {

    String code();

    ProcessingResult process(ProcessingJob job);
}
