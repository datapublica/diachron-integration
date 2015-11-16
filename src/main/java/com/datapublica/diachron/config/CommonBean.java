package com.datapublica.diachron.config;

import com.datapublica.common.http.DPHttpClient;
import com.datapublica.common.http.impl.DPHttpClientImpl;
import org.athena.imis.diachron.archive.datamapping.MultidimensionalConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@Configuration
public class CommonBean {

    @Bean
    public MultidimensionalConverter converter() {
        return new MultidimensionalConverter();
    }

    @Bean
    public DPHttpClient httpClient() {
        return new DPHttpClientImpl();
    }
}
