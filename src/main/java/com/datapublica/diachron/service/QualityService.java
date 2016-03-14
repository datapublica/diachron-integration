package com.datapublica.diachron.service;

import com.datapublica.common.http.DPHttpClient;
import com.datapublica.common.http.util.HttpUriRequestUtil;
import com.datapublica.diachron.config.DiachronConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.http.client.methods.HttpPost;
import org.athena.imis.diachron.archive.datamapping.utils.BulkLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by loic on 22/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Service
public class QualityService {
    private static ObjectMapper om = new ObjectMapper();

    @Autowired
    private DPHttpClient http;
    @Autowired
    private DiachronConfig config;

    @Autowired
    private ArchiveService archive;

    public static class QualityRequest {
        @JsonProperty("@id") // nothing like an actual id...
        public String id = "_:" + new Random(System.currentTimeMillis()).ints(40000000, 80000000);

        @JsonProperty("@type")
        public List<String> type = Collections.singletonList("http://purl.org/eis/vocab/lmi#MetricConfiguration");

        @JsonProperty("http://purl.org/eis/vocab/lmi#metric")
        public List<Map<String, String>> metrics = new LinkedList<>();

        public QualityRequest addMetric(String metric) {
            metrics.add(ImmutableMap.of("@value", metric));
            return this;
        }
    }

    public void computeQualityMetrics(String dsId, byte[] dataset) throws IOException {
        Path tempFile = Files.createTempFile(new File("/tmp").toPath(), "quality-", ".ttl");
        try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
            fos.write(dataset);
            fos.close();

            QualityRequest metrics = new QualityRequest().addMetric("eu.diachron.qualitymetrics.datapublica.completeness.DataCubePopulationCompleteness").addMetric("eu.diachron.qualitymetrics.intrinsic.consistency.UsageOfDeprecatedClassesOrProperties");
            ImmutableMap<String, String> data = ImmutableMap.of("IsSparql", "false", "BaseUri", dsId, "Dataset", tempFile.toAbsolutePath().toString(), "QualityReportRequired", "false", "MetricsConfiguration", om.writeValueAsString(metrics));

            HttpPost request = new HttpPost(config.getQualityBaseUrl());
            HttpUriRequestUtil.setParams(request, data);
            http.execute(request);

            String qualityReportFile = config.getQualityReport(dsId);
            try (FileInputStream fis = new FileInputStream(qualityReportFile)) {
                if (!BulkLoader.bulkLoadRDFDataToGraph(fis, dsId+"/quality", "trig")) {
                    throw new IllegalStateException("Impossible to load quality report");
                }
            }
        } finally {
            Files.delete(tempFile);
        }
    }

    public List<Map<String, Object>> getQuality(String datasetVersion) throws IOException {
        String query = "select ?group ?category ?type ?value ?date \n" +
                "from <"+datasetVersion+"/quality> {\n" +
                "    ?g a <http://purl.org/eis/vocab/daq#QualityGraph>.\n" +
                "    GRAPH ?g {\n" +
                "        ?obs a <http://purl.org/linked-data/cube#Observation> ; \n" +
                "            <http://purl.org/eis/vocab/daq#metric> ?instance ; \n" +
                "            <http://purl.org/eis/vocab/daq#value> ?value ; \n" +
                "            <http://purl.org/linked-data/sdmx/2009/dimension#timePeriod> ?date.\n" +
                "        ?instance a ?type.\n" +
                "        ?category_instance a ?category ; \n" +
                "            ?p ?instance.\n" +
                "        OPTIONAL { ?group_instance a ?group; \n" +
                "            ?p1 ?category_instance. FILTER(STRSTARTS(STR(?p1), \"http://www.diachron-fp7.eu/dqm\")) } \n" +
                "        FILTER(STRSTARTS(STR(?p), \"http://www.diachron-fp7.eu/dqm\"))\n" +
                "    }\n" +
                "}";
        List<Map<String, Object>> result = archive.querySelect(query);
        result.forEach(this::asHumanReadable);
        return result;
    }

    private void asHumanReadable(Map<String, Object> it) {
        replaceDQM(it, "category");
        replaceDQM(it, "type");
        replaceDQM(it, "group");
    }

    private void replaceDQM(Map<String, Object> it, String attr) {
        Object o = it.get(attr);
        if (o == null) return;
        it.put(attr, o.toString().replace("http://www.diachron-fp7.eu/dqm#", ""));
    }
}
