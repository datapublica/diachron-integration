package com.datapublica.diachron.api;

import com.datapublica.diachron.service.ArchiveService;
import com.datapublica.diachron.service.data.*;
import com.datapublica.diachron.util.CounterMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

/**
 * @author Jacques Belissent
 */
@Controller
@RequestMapping("/api/changes")
public class ChangesApi {

    @Autowired
    private ArchiveService archive;

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
        changes.forEach(d -> typeFacet.add(d.getType(), 1L));

        final CounterMap<String> measureFacet = new CounterMap<>();
        facets.put("measure", measureFacet);
        changes.stream().filter(d -> d.getType().getParameters().contains("measure"))
                .forEach(d -> measureFacet.add((String) d.getProperties().get("measure"), 1L));

        final CounterMap<String> dimensionFacets = new CounterMap<>();
        facets.put("dimension", dimensionFacets);
        changes.stream().filter(d -> d.getType().getParameters().contains("dimension"))
                .forEach(d -> measureFacet.add((String) d.getProperties().get("dimension"), 1L));

        // added and removed observations affect all dimensions or measures so add them all
        /*
        changes.stream().filter(d -> d.getType() == Difference.Type.ADD_OBSERVATION || d.getType() == Difference.Type.DELETE_OBSERVATION)
                .forEach(d -> {
                    measureFacet.put((String)d.getProperties().get("measure"), 1L);
                    measureFacet.put((String)d.getProperties().get("dimension"), 1L);
                });
*/
        return facets;
    }

    @ResponseBody
    @RequestMapping(value = "/{name}/meta", method = RequestMethod.POST, produces = "application/json")
    public ChangeSetResponse searchSchemaChanges(@PathVariable String name,
                                    @RequestParam (value = "fromVersion", required = false) String fromVersion,
                                    @RequestParam (value = "toVersion", required = false) String toVersion,
                                    @RequestParam(value = "p", required = false, defaultValue = "0") Integer offset,
                                    @RequestParam(value = "s", required = false, defaultValue = "10") Integer limit,
                                    @RequestBody Map<String, Object> filter) throws IOException {
        return archive.searchChanges(name, true,
                fromVersion, toVersion, offset, limit,
                filter);
    }


    @ResponseBody
    @RequestMapping(value = "/{name}/data", method = RequestMethod.POST, produces = "application/json")
    public ChangeSetResponse searchDataChanges(@PathVariable String name,
                                               @RequestParam (value = "fromVersion", required = false) String fromVersion,
                                               @RequestParam (value = "toVersion", required = false) String toVersion,
                                               @RequestParam(value = "p", required = false, defaultValue = "0") Integer offset,
                                               @RequestParam(value = "s", required = false, defaultValue = "10") Integer limit,
                                               @RequestBody Map<String, Object> filter) throws IOException {
        return archive.searchChanges(name, false,
                fromVersion, toVersion, offset, limit,
                filter);
    }


    @ResponseBody
    @RequestMapping(value = "/{name}/data", method = RequestMethod.DELETE, produces = "application/json")
    public void hideDataTuple(@PathVariable String name, @RequestParam String key, @RequestParam String value) throws IOException {
        Map<String, Object> m = new HashMap<>();
        m.put(key, value);
        if ("type".equals(key)) {
            archive.hideChangeType(Difference.Type.valueOf(value));
        } else {
            archive.hideChangeProperties(name, false, m);
        }
    }


    @ResponseBody
    @RequestMapping(value = "/{name}/meta", method = RequestMethod.DELETE, produces = "application/json")
    public void hideMetaTuple(@PathVariable String name, @RequestParam String key, @RequestParam String value) throws IOException {
        Map<String, Object> m = new HashMap<>();
        m.put(key, value);
        if ("type".equals(key)) {
            archive.hideChangeType(Difference.Type.valueOf(value));
        } else {
            archive.hideChangeProperties(name, true, m);
        }
    }

}
