package com.datapublica.diachron.service;

import com.datapublica.common.http.DPHttpClient;
import com.datapublica.diachron.config.DiachronConfig;
import com.datapublica.diachron.service.data.DatasetVersion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * Created by loic on 22/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Service
public class ChangeService {
    private static ObjectMapper om = new ObjectMapper();

    @Autowired
    private DPHttpClient http;
    @Autowired
    private DiachronConfig config;

    @Autowired
    private ArchiveService archive;

    public void computeChangeSet(String dataset, DatasetVersion versionOld, DatasetVersion versionNew, boolean recordset) throws IOException {
        String type = recordset ? "recordset" : "schemaset";
        ImmutableMap<String, Object> data = ImmutableMap.of(
                "Dataset_URI", "http://www.diachron-fp7.eu/resource/"+type+"/"+dataset,
                "Old_Version", recordset ? versionOld.recordSet : versionOld.schemaSet,
                "New_Version", recordset ? versionNew.recordSet : versionNew.schemaSet,
                "Ingest", true,
                "Complex_Changes", Collections.emptyList()
        );

        HttpPost request = new HttpPost(config.getChangeBaseUrl());
        request.setEntity(new StringEntity(om.writeValueAsString(data), ContentType.APPLICATION_JSON));
        http.execute(request);
    }
}
