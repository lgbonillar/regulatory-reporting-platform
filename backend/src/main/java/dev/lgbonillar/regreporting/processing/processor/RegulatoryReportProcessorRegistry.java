package dev.lgbonillar.regreporting.processing.processor;

import dev.lgbonillar.regreporting.processing.processor.demo.DemoRegulatoryReportProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegulatoryReportProcessorRegistry {

    private final List<RegulatoryReportProcessor> processors;

    public RegulatoryReportProcessorRegistry(List<RegulatoryReportProcessor> processors) {
        this.processors = processors;
    }

    public RegulatoryReportProcessor getDefaultProcessor() {
        return processors.stream()
                .filter(processor -> processor.code().equals(DemoRegulatoryReportProcessor.PROCESSOR_CODE))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Default report processor is not configured"));
    }
}
