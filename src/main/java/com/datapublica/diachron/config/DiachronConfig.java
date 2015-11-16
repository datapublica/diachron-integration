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

    public String getArchiveBaseUrl() {
        return archiveBaseUrl;
    }
}
