package dev.lgbonillar.regreporting.processing.processor.demo;

import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;
import dev.lgbonillar.regreporting.processing.processor.ProcessingResult;
import dev.lgbonillar.regreporting.processing.processor.RegulatoryReportProcessor;
import org.springframework.stereotype.Component;

@Component
public class DemoRegulatoryReportProcessor implements RegulatoryReportProcessor {

    public static final String PROCESSOR_CODE = "DEMO_REGULATORY_REPORT";

    @Override
    public String code() {
        return PROCESSOR_CODE;
    }

    @Override
    public ProcessingResult process(ProcessingJob job) {
        return ProcessingResult.successful(
                PROCESSOR_CODE,
                "Demo regulatory report processed successfully"
        );
    }
}
