package com.datapublica.diachron.api;

import com.datapublica.diachron.service.ArchiveService;
import com.datapublica.diachron.service.data.*;
import com.datapublica.diachron.util.CounterMap;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

/**
 * @author Jacques Belissent
 */
@Controller
@RequestMapping("/api/meta")
public class MetadataApi {

    private Dataset animals;

    ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    private ArchiveService archive;

    @Autowired
    private FactApi facts;

    @PostConstruct
    private void createFakeDataset() {
        this.animals = new Dataset();
        animals.setUri("animals");
        animals.setName("Animals");
        animals.setCreationDate(date(2015, 3, 3));

        Concept name = new Concept("name", "Name", Concept.BasicType.STRING);
        Codelist color = new Codelist("color", "Color", Concept.BasicType.STRING);
        color.setInstances("pink", "white", "brown", "black");
        Concept count = new Codelist("count", "Count", Concept.BasicType.INTEGER);

        DatasetVersion version = new DatasetVersion("0", null, null, date(2015, 3, 3));
        version.addDimension(name).addDimension(color).addMeasure(count);
        animals.addVersion(version);

        version = new DatasetVersion("1", null, null, date(2015, 9, 3));
        color = new Codelist("color", "Color", Concept.BasicType.STRING);
        color.setInstances("red", "white", "brown", "black");
        version.addDimension(name).addDimension(color).addMeasure(count);
        animals.addVersion(version);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public Collection<Dataset> getDatasets() throws IOException {
        return Collections.singletonList(getDataset("maires"));
    }

    @ResponseBody
    @RequestMapping(value = "{name}", method = RequestMethod.GET, produces = "application/json")
    public Dataset getDataset(@PathVariable String name) throws IOException {
        String id = archive.getDiachronicDSByName(name);
        Dataset result = new Dataset();
        result.setName(name);
        result.setUri(shortenId(id));
        result.setVersions(archive.getDatasetVersions(id).stream().map(v -> {
            try {
                v.setModel(getModel(id));
            } catch (IOException e) {
                e.printStackTrace();
            }
            v.id = shortenId(v.id);
            return v;
        }).collect(Collectors.toList()));
        return result;
    }

    private Object getModel(String id) throws IOException {
        Model model = archive.getDatasetMetaData(id, null);
        String json = archive.serializeModel(model, "RDF/JSON");
        return jsonMapper.readValue(json, Object.class);
    }

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
    @RequestMapping(value = "/changes/{name}", method = RequestMethod.POST, produces = "application/json")
    public ChangeSetResponse search(@PathVariable String name,
                                    @RequestParam (value = "fromVersion", required = false) String fromVersion,
                                    @RequestParam (value = "toVersion", required = false) String toVersion,
                                    @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
                                    @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit,
                                    @RequestBody Map<String, Object> filter) throws IOException {
        /*
        "Could not write content: Null key for a Map not allowed in JSON (use a converting NullKeySerializer?); nested exception is com.fasterxml.jackson.core.JsonGenerationException: Null key for a Map not allowed in JSON (use a converting NullKeySerializer?)"
        final List<DatasetVersion> versions = service.getDatasetVersions(service.getDiachronicDSByName(id)).stream()
                .filter(it -> it.date.compareTo(new Date(fromVersion)) >= 0 && it.date.compareTo(new Date(toVersion)) <= 0)
                .collect(Collectors.toList());

        final Model changeSet = service.getChangeSet(versions.get(0).recordSet, versions.get(versions.size() - 1).recordSet);
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

        */

        ChangeSetQuery query  = new ChangeSetQuery();

        if (filter != null) {
            for (Map.Entry<String, Object> entry : filter.entrySet()) {
                if ("type".equals(entry.getKey())) {
                    query.setType(Difference.Type.valueOf(entry.getValue().toString()));
                } else {
                    query.setProperty(entry.getKey(), entry.getValue());
                }
            }
        }

        String id = archive.getDiachronicDSByName(name);

        List<DatasetVersion> versions = archive.getDatasetVersions(id);
        if (fromVersion == null) {
            fromVersion = versions.get(0).recordSet;
        }
        if (toVersion == null) {
            toVersion = versions.get(versions.size()-1).recordSet;
        }

        return archive.getChangeSetResult("recordset/"+name,
                fromVersion, toVersion,
                query);
    }



    private String restoreId(String uri)  {
        return ArchiveService.RESOURCE_BASE_URI + uri.replaceAll(":", ":");
    }

    private String shortenId(String uri)  {
        return uri.replace(ArchiveService.RESOURCE_BASE_URI, "")
                .replaceAll("\\/", ":");
    }

    private java.util.Date date(int year, int month, int dayOfMonth) {
        return Date.from(LocalDate.of(year, month, dayOfMonth).atStartOfDay().toInstant(ZoneOffset.UTC));
    }
}
