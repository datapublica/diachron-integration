package com.datapublica.diachron.api;

import com.datapublica.diachron.service.ArchiveService;
import com.datapublica.diachron.service.ChangeService;
import com.datapublica.diachron.service.QualityService;
import com.datapublica.diachron.service.data.ChangeSetQuery;
import com.datapublica.diachron.service.data.ChangeSetResponse;
import com.datapublica.diachron.service.data.DatasetVersion;
import com.datapublica.diachron.service.data.Difference;
import com.hp.hpl.jena.rdf.model.*;
import org.athena.imis.diachron.archive.datamapping.MultidimensionalConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Controller
@RequestMapping("/api/archive")
public class ArchiveApi {

    @Autowired
    private MultidimensionalConverter converter;

    @Autowired
    private ArchiveService service;
    @Autowired
    private QualityService qualityService;

    @Autowired
    private ChangeService changeService;

    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = "application/turtle")
    String save(@RequestBody byte[] body, @PathVariable String id) throws Exception {

        String dds = service.getDiachronicDSByName(id);
        if (dds == null)
            dds = service.createDiachronicDataset(id);

        Model diachronModel = converter.convert(new ByteArrayInputStream(body), "ttl", id);

        String dsUri = service.putDataset(dds, diachronModel);
        qualityService.computeQualityMetrics(dsUri, body);
        return dsUri;
    }

    @ResponseBody
    @RequestMapping(value = "/{id}/changes", method = RequestMethod.POST, produces = "text/plain")
    String getId(@PathVariable String id, @RequestParam boolean recordset) throws IOException {
        List<DatasetVersion> versions = service.getDatasetVersions(getId(id));
        changeService.computeChangeSet(id, versions.get(1), versions.get(0), recordset);
        return "ok";
    }

    @ResponseBody
    @RequestMapping(value = "/{id}/quality", method = RequestMethod.GET, produces = "application/json")
    List<Map<String, Object>> getQuality(@PathVariable String id, @RequestParam(required = false) String versionId) throws IOException {
        List<DatasetVersion> versions = getDatasets(id);
        DatasetVersion datasetVersion = null;
        if (versionId != null) {
            datasetVersion = versions.stream().filter(it -> it.id.equals(versionId)).findFirst().orElse(null);
        }
        if (datasetVersion == null) {
            datasetVersion = versions.get(0);
        }
        return qualityService.getQuality(datasetVersion.id);
    }

    @ResponseBody
    @RequestMapping(value = "/{id}/dds", method = RequestMethod.GET, produces = "text/plain")
    String getId(@PathVariable String id) throws IOException {
        return  service.getDiachronicDSByName(id);
    }

    @ResponseBody
    @RequestMapping(value = "/{id}/versions", method = RequestMethod.GET)
    List<DatasetVersion> getDatasets(@PathVariable String id) throws IOException {
        return  service.getDatasetVersions(getId(id));
    }

    @ResponseBody
    @RequestMapping(value = "/{id}/meta", method = RequestMethod.GET, produces = "text/plain")
    String getValue(@PathVariable String id,
                    @RequestParam(required = false) String objectType,
                    @RequestParam(required = false) String objectId,
                    @RequestParam(required = false) Long at) throws IOException {
        if (objectType == null && objectId != null || objectId == null && objectType != null) {
            throw new IllegalArgumentException("Cannot make objectType not null and objectId null (or the other way around)");
        }
        Model model = service.getDatasetMetaData(getId(id), at, service.getConceptUri(objectType, objectId));
        return service.serializeModel(model);
    }

    @ResponseBody
    @RequestMapping(value = "/{id}/data/changes", method = RequestMethod.GET)
    ChangeSetResponse getChanges(@PathVariable String id, @RequestParam long from,
                      @RequestParam long to,
                      @RequestParam(required = false) Difference.Type type,
                                 @RequestParam(required = false) Difference.Type joinType) throws IOException {
        final List<DatasetVersion> versions = service.getDatasetVersions(getId(id)).stream().filter(it -> it.date.compareTo(new Date(from)) >= 0 && it.date.compareTo(new Date(to)) <= 0).collect(Collectors.toList());
        return service.getChangeSetResult("recordset/" + id, versions.get(0).recordSet, versions.get(versions.size() - 1).recordSet, new ChangeSetQuery(type, joinType));
    }

    @ResponseBody
    @RequestMapping(value = "/{id}/meta/changes", method = RequestMethod.GET)
    ChangeSetResponse getChangesMeta(@PathVariable String id, @RequestParam long from,
                                     @RequestParam long to,
                                     @RequestParam(required = false) Difference.Type type,
                                     @RequestParam(required = false) Difference.Type joinType) throws IOException {
        final List<DatasetVersion> versions = service.getDatasetVersions(getId(id)).stream().filter(it -> it.date.compareTo(new Date(from)) >= 0 && it.date.compareTo(new Date(to)) <= 0).collect(Collectors.toList());
        return service.getChangeSetResult("schemaset/" + id, versions.get(0).recordSet, versions.get(versions.size() - 1).recordSet, new ChangeSetQuery(type, joinType));
    }


    @ResponseBody
    @RequestMapping(value = "/{id}/data", method = RequestMethod.GET, produces = "text/plain")
    String getData(@PathVariable String id,
                   @RequestParam(required = false) String dimensionType,
                   @RequestParam(required = false) String dimensionObject,
                   @RequestParam(required = false) Long at) throws IOException {
        if (dimensionType == null && dimensionObject != null || dimensionObject == null && dimensionType != null) {
            throw new IllegalArgumentException("Cannot make dimensionObject not null and dimensionType null (or the other way around)");
        }
        Model model = service.getDatasetData(service.getFit(getId(id), at).id, service.getConceptUri(dimensionType, dimensionObject));
        return service.serializeModel(model);
    }

}
