package com.datapublica.diachron.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@Configuration
public class DiachronConfig {
    @Value("${archive.base:http://localhost:7090/archive-web-services}")
    private String archiveBaseUrl;
    @Value("${quality.base:http://localhost:5677/Luzzu/compute_quality}")
    private String integrationPlatform;
    @Value("${quality.report.dir:~/luzzu/quality-metadata/}")
    private String qualityReportDir;

    public String getArchiveBaseUrl() {
        return archiveBaseUrl;
    }

    public String getIntegrationPlatform() {
        return integrationPlatform;
    }

    public String getQualityReport(String baseUri) {
        return qualityReportDir.replaceFirst("^~",System.getProperty("user.home"))+ "/" + baseUri.replace("http://", "")+"/quality-meta-data.trig";
    }
}
