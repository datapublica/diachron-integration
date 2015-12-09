package com.datapublica.diachron.api;

import com.datapublica.diachron.service.ArchiveService;
import com.datapublica.diachron.service.data.Difference;
import com.datapublica.diachron.util.CounterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * API for retrieving changes between versions of a dataset
 *
 * @author Jacques Belissent
 */
@Controller
@RequestMapping("/api/changes")
public class ChangesApi {


    @Autowired
    private ArchiveService service;

    @Autowired
    private FactApi facts;

    @ResponseBody
    @RequestMapping(value = "/{id}/summary", method = RequestMethod.GET, produces = "application/json")
    public Map getSummary(@PathVariable String id,
                   @RequestParam long fromVersion,
                   @RequestParam long toVersion) throws IOException {
        /*
        final List<DatasetVersion> versions = service.getDatasetVersions(service.getDiachronicDSByName(id)).stream()
                .filter(it -> it.date.compareTo(new Date(fromVersion)) >= 0 && it.date.compareTo(new Date(toVersion)) <= 0)
                .collect(Collectors.toList());

        final Model changeSet = service.getChangeSet(versions.get(0).recordSet, versions.get(versions.size() - 1).recordSet);
*/
        Map response = new HashMap<>();
        response.put("total", facts.changes.size());
        response.put("offset", 0);
        response.put("size", 0);
        response.put("facets", createFacets(facts.changes));

        return response;
    }

    private Map createFacets(List<Difference> changes) {
        Map facets = new HashMap<>();

        final CounterMap<Difference.Type> typeFacet = new CounterMap<>();
        facets.put("type", typeFacet);
        changes.forEach(d -> typeFacet.put(d.getType(), 1L));

        final CounterMap<String> measureFacet = new CounterMap<>();
        facets.put("measure", measureFacet);
        changes.stream().filter(d -> d.getType().getParameters().contains("measure"))
                .forEach(d -> measureFacet.put((String)d.getProperties().get("measure"), 1L));

        final CounterMap<String> dimensionFacets = new CounterMap<>();
        facets.put("dimension", dimensionFacets);
        changes.stream().filter(d -> d.getType().getParameters().contains("dimension"))
                .forEach(d -> measureFacet.put((String) d.getProperties().get("dimension"), 1L));

        return facets;
    }

    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
    public Map search(@PathVariable String id,
                   @RequestParam String fromVersion,
                   @RequestParam String toVersion,
                   @RequestParam(value = "type", required = false) Set<Difference.Type> type,
                   @RequestParam(value = "measure", required = false) Set<String> measure,
                   @RequestParam(value = "dimension", required = false) Set<String> dimension,
                   @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
                   @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) throws IOException {
        /*"Could not write content: java.lang.Integer cannot be cast to java.lang.Long (through reference chain: java.util.HashMap["facets"]->java.util.HashMap["type"]->com.datapublica.diachron.util.CounterMap["ATTACH_INSTANCE_TO_CODELIST"]); nested exception is com.fasterxml.jackson.databind.JsonMappingException: java.lang.Integer cannot be cast to java.lang.Long (through reference chain: java.util.HashMap["facets"]->java.util.HashMap["type"]->com.datapublica.diachron.util.CounterMap["ATTACH_INSTANCE_TO_CODELIST"])"
        final List<DatasetVersion> versions = service.getDatasetVersions(service.getDiachronicDSByName(id)).stream()
                .filter(it -> it.date.compareTo(new Date(fromVersion)) >= 0 && it.date.compareTo(new Date(toVersion)) <= 0)
                .collect(Collectors.toList());

        final Model changeSet = service.getChangeSet(versions.get(0).recordSet, versions.get(versions.size() - 1).recordSet);
*/
        Map response = new HashMap<>();

        List<Difference> subset = facts.changes.stream()
                .filter(d -> (type == null || type.contains(d.getType())))
                .filter(d -> (dimension == null || dimension.contains(d.getProperty("dimension"))))
                .filter(d -> (measure == null || measure.contains(d.getProperty("measure"))))
                .collect(Collectors.toList());

        List<Difference> result = subset.subList(offset, Math.min(subset.size(), offset + limit));
        response.put("total", facts.changes.size());
        response.put("offset", 0);
        response.put("size", result.size());
        response.put("facets", createFacets(subset));
        response.put("data", result);

        return response;
    }


}
