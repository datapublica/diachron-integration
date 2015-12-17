package com.datapublica.diachron.service;

import com.datapublica.common.http.DPHttpClient;
import com.datapublica.common.http.util.HttpUriRequestUtil;
import com.datapublica.diachron.config.DiachronConfig;
import com.datapublica.diachron.service.data.*;
import com.datapublica.diachron.util.StreamUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.hp.hpl.jena.rdf.model.*;
import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.athena.imis.diachron.archive.core.dataloader.RDFDictionary;
import org.athena.imis.diachron.archive.models.DiachronOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class ArchiveService {
    private static final Logger log = LoggerFactory.getLogger(ArchiveService.class);

    public static final String DICTIONARY = "http://www.diachron-fp7.eu/archive/dictionary";
    public static final String RESOURCE_BASE_URI = "http://www.diachron-fp7.eu/resource/";

    public static final String BASE_PUBLICATION_URI = "http://www.data-publica.com/lod/publication/";

    @Autowired
    private DPHttpClient http;
    @Autowired
    private DiachronConfig config;

    public String getConceptUri(String type, String value) {
        if (type == null)
            return null;
        type = type.replace(':', '#');
        String uri = BASE_PUBLICATION_URI + type;
        if (value != null)
            uri += "-value-" + value;
        return uri;
    }

    /**
     * @param id
     * @return
     * @throws IOException
     */
    public String getDiachronicDSByName(String id) throws IOException {
        HttpGet get = new HttpGet(config.getArchiveBaseUrl() + "/archive");
        String query = "SELECT ?dataset FROM <" + DICTIONARY + "> " +
                "WHERE {?dataset a <http://www.diachron-fp7.eu/resource/DiachronicDataset> ; <http://purl.org/dc/terms/title> \"" + id + "\"}";
        HttpUriRequestUtil.setParams(get, ImmutableMap.of("query", query, "queryType", "SELECT"));
        return fetchOne(get, "dataset");
    }

    /**
     * Put a new version of the given datacube dataset
     *
     * @param ddsId
     * @param model
     * @return
     * @throws IOException
     */
    public String putDataset(String ddsId, Model model) throws IOException {
        HttpPost post = new HttpPost(config.getArchiveBaseUrl() + "/archive/dataset/version");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.write(out, "N3");


        byte[] data = out.toByteArray();

        ByteArrayBody file = new ByteArrayBody(data, "import.ttl");
        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("diachronicDatasetURI", new StringBody(ddsId, ContentType.TEXT_PLAIN))
                .addPart("format", new StringBody("TURTLE", ContentType.TEXT_PLAIN))
                .addPart("dataFile", file)
                .build();

        post.setEntity(reqEntity);

        return http.execute(post).json().get("data").asText();
    }

    public List<DatasetVersion> getDatasetVersions(String dds) throws IOException {
        JsonNode results = queryTemplate("listDatasets", "diachronicDatasetId", dds).get("results").get("bindings");
        return StreamUtil.stream(results).map(it -> {
            String creationTime = it.get("creationTime").get("value").asText();
            String recordSet = it.get("recordSet").get("value").asText();
            String schemaSet = it.get("schemaSet").get("value").asText();
            String dataset = it.get("dataset").get("value").asText();

            return new DatasetVersion(dataset, recordSet, schemaSet, Date.from(ZonedDateTime.parse(creationTime).toInstant()));
        }).sorted((a, b) -> -a.date.compareTo(b.date)).collect(Collectors.toList());
    }


    public Model getDatasetMetaData(String datasetId, String subjectRestrictionURI) throws IOException {
        String queryString = "CONSTRUCT {?s ?p ?o} " +
                "WHERE {{" +
                "SELECT ?s ?p ?o FROM <" + datasetId + "> WHERE {?s ?p ?o}}}";
        if (subjectRestrictionURI != null) {
            queryString = queryString.replace("?s", "<" + subjectRestrictionURI + ">");
        }
        return query(queryString);
    }

    public Model getDatasetData(String datasetId, String dimensionRestrictionURI) throws IOException {
        return query("CONSTRUCT {?s ?p ?o} " +
                "WHERE {{" +
                "SELECT ?s ?p ?o FROM <" + RDFDictionary.getDictionaryNamedGraph() + "> " +
                "WHERE " +
                "{ <" + datasetId + "> <" + DiachronOntology.hasRecordSet + "> ?rs ." +
                "GRAPH ?rs {" +
                "{?rec <" + DiachronOntology.subject + "> ?s ; " +
                "<" + DiachronOntology.hasRecordAttribute + "> [<" + DiachronOntology.predicate + "> ?p ; <" + DiachronOntology.object + "> ?o]" +
                (dimensionRestrictionURI != null ? " ; <" + DiachronOntology.hasRecordAttribute + "> [<" + DiachronOntology.object + "> <" + dimensionRestrictionURI + ">]" : "") +
                "}" +
                " UNION {?s a <" + DiachronOntology.multidimensionalObservation + "> ; <" + DiachronOntology.hasRecordAttribute + "> [<" + DiachronOntology.predicate + "> ?p ; <" + DiachronOntology.object + "> ?o] " + (dimensionRestrictionURI != null ? " ; <" + DiachronOntology.hasRecordAttribute + "> [<" + DiachronOntology.object + "> <" + dimensionRestrictionURI + ">]" : "") + "}" +
                "}" +
                "}" +
                "}" +
                "}");
    }

    private Model jsonResultsToModel(JsonNode results) {
        Model model = ModelFactory.createDefaultModel();
        results.fields().forEachRemaining(statement -> {
            Resource subject = model.createResource(statement.getKey());
            JsonNode predicates = statement.getValue();
            predicates.fields().forEachRemaining(predicateEntry -> {
                Property property = model.createProperty(predicateEntry.getKey());
                JsonNode objects = predicateEntry.getValue();
                for (JsonNode object : objects) {
                    RDFNode value;
                    switch (object.get("type").asText()) {
                        case "uri":
                            value = model.createResource(object.get("value").asText());
                            break;
                        case "literal":
                            value = model.createLiteral(object.get("value").asText());
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown data type " + object.get("type").asText());
                    }
                    subject.addProperty(property, value);
                }
            });
        });

        return model;
    }

    private List<Map<String, Object>> jsonResultsToMap(JsonNode results) {
        List<Map<String, Object>> res = new LinkedList<>();
        for (JsonNode binding : results.path("results").path("bindings")) {
            Map<String, Object> row = new HashMap<>();
            res.add(row);
            final Iterable<String> fields = binding::fieldNames;
            for (String field : fields) {
                final JsonNode valueNode = binding.get(field);
                final String valueAsString = valueNode.get("value").asText();
                Object value;
                switch (valueNode.get("type").asText()) {
                    case "uri":
                        value = URI.create(valueAsString);
                        break;
                    case "typed-literal":
                        String type = valueNode.get("datatype").asText();
                        if (type.contains("integer")) {
                            value = Integer.valueOf(valueAsString);
                        } else if (type.contains("float") || type.contains("double")) {
                            value = Double.valueOf(valueAsString);
                        } else if (type.contains("bool") || type.contains("double")) {
                            value = Boolean.valueOf(valueAsString);
                        } else {
                            value = valueAsString;
                        }
                        break;
                    default:
                        value = valueAsString;
                }
                row.put(field, value);
            }
        }

        return res;
    }

    private JsonNode queryTemplate(String template, String... parameters) throws IOException {
        HttpGet get = new HttpGet(config.getArchiveBaseUrl() + "/archive/templates");
        final ImmutableMap<String, String> params;
        if (parameters.length == 2) {
            params = ImmutableMap.of("name", template, parameters[0], parameters[1]);
            log.info("Querying template " + template + " using " + parameters[0] + "=" + parameters[1]);
        } else if (parameters.length == 4) {
            params = ImmutableMap.of("name", template, parameters[0], parameters[1], parameters[2], parameters[3]);
            log.info("Querying template " + template + " using " + parameters[0] + "=" + parameters[1] + ", " + parameters[2] + "=" + parameters[3]);
        } else {
            throw new IllegalArgumentException("Illegal parameter size " + parameters.length);
        }
        HttpUriRequestUtil.setParams(get, params);
        return fetch(get);
    }

    public String createDiachronicDataset(String id) throws IOException {
        HttpPost post = new HttpPost(config.getArchiveBaseUrl() + "/archive/dataset");
        HttpUriRequestUtil.setParams(post, ImmutableMap.of("datasetName", id, "label", id, "creator", "datapublica"));
        return http.execute(post).json().get("data").asText();
    }

    private String fetchOne(HttpUriRequest get, String field) throws IOException {
        JsonNode json = http.execute(get).json();
        JsonNode binding = json.get("data").get("results").get("bindings");
        if (binding.size() != 1) {
            return null;
        } else {
            return binding.get(0).get(field).get("value").asText();
        }
    }

    public Model getChangeSet(String datasetBaseURI, String newVersion, String oldVersion) throws IOException {
        String changeset = RESOURCE_BASE_URI + datasetBaseURI + "/changes/" + oldVersion.replaceFirst(".*/([^/]*)$", "$1") + "-" + newVersion.replaceFirst(".*/([^/]*)$", "$1");
        String query = "CONSTRUCT {?s ?p ?o} WHERE {{" + "SELECT ?s ?p ?o FROM <" + changeset + "> WHERE {?s ?p ?o}}}";
        return query(query);
    }

    public ChangeSetResponse getChangeSetResult(String datasetBaseURI, String newVersion, String oldVersion, ChangeSetQuery query) throws IOException {
        String changeset = RESOURCE_BASE_URI + datasetBaseURI + "/changes/" + oldVersion.replaceFirst(".*/([^/]*)$", "$1") + "-" + newVersion.replaceFirst(".*/([^/]*)$", "$1");
        final Difference.Type type = query.getType();
        String conditions = "?change a ?type";
        Set<String> uris = new HashSet<>();
        if (type != null) {
            List<String> parameters = type.getParameters();
            final int parameterSize = parameters.size();
            for (int i = 1; i <= parameterSize; i++) {
                conditions += "; co:" + type.getParameterId(i) + " ?p" + i;
            }
            conditions += ".\n";
            conditions += "?change a co:" + type.getUriName() + ".\n";

            for (Map.Entry<String, Object> entry : query.getProperties().entrySet()) {
                final String property = entry.getKey();
                final String parameterId = type.getParameterId(type.getParameterIdFromName(property));
                conditions += "?change co:" + parameterId + " ";
                final Object value = entry.getValue();
                if (value.toString().startsWith("http://")) {
                    conditions += "<" + value + ">.\n";
                } else {
                    conditions += "\"" + value + "\".\n";
                }
            }

            if (query.getJoinType() != null) {
                conditions += "?change_join a co:" + query.getJoinType().getUriName() + " ; co:" + query.getJoinType().getParameterId(1) + " ?p1.\n";
            }
        } else {
            conditions += ".\n";
            if (query.getJoinType() != null || !query.getProperties().isEmpty()) {
                throw new IllegalStateException("Not implemented yet");
            }
        }

        String prefix = "PREFIX co: <http://www.diachron-fp7.eu/changes/>\n";
        final String queryByType = prefix + "SELECT ?type (COUNT(DISTINCT ?change) AS ?ns) FROM <" + changeset + "> WHERE {" + conditions + "} GROUP BY ?type ORDER BY DESC(?ns)";
        Map<Difference.Type, Long> byType = querySelect(queryByType).stream().collect(Collectors.toMap(it -> Difference.Type.fromUri(it.get("type")), it -> ((Number) it.get("ns")).longValue(), (a, b) -> a, LinkedHashMap::new));
        final ChangeSetResponse.Facets facets = new ChangeSetResponse.Facets();
        facets.setTypes(byType);
        final Long count = byType.values().stream().collect(Collectors.reducing(Long::sum)).orElse(0L);
        final ChangeSetResponse response = new ChangeSetResponse();
        response.setTotal(count);
        response.setFacets(facets);

        if (type != null) {
            List<String> parameters = type.getParameters();
            final int parameterSize = parameters.size();
            for (int i = 1; i <= parameterSize; i++) {
                String param = parameters.get(i - 1);

                String p = "p" + i;
                String queryStr = prefix + "SELECT ?" + p + " (COUNT(DISTINCT ?change) AS ?ns) FROM <" + changeset + "> WHERE {" + conditions + "} GROUP BY ?" + p + " ORDER BY DESC(?ns) LIMIT 20";

                List<Map<String, Object>> results = querySelect(queryStr);
                facets.getParameters().put(param, results
                        .stream().peek(it -> {
                            String pValue = it.get(p).toString();
                            if (pValue.startsWith("http")) uris.add(pValue);
                        }).collect(Collectors.toMap(it -> it.get(p).toString(), it -> ((Number) it.get("ns")).longValue(), (a, b) -> a, LinkedHashMap::new)));
            }
            String queryStr = prefix + "SELECT ?type_join (COUNT(DISTINCT ?change) AS ?ns) FROM <" + changeset + "> WHERE {" + conditions + " ?change co:" + type.getParameterId(1) + " ?join. ?change_join a ?type_join ; ?unknown_p1 ?join. FILTER(?type != ?type_join && STRENDS(str(?unknown_p1), \"_p1\"))} GROUP BY ?type_join ORDER BY DESC(?ns) LIMIT 20";

            facets.setJoinTypes(querySelect(queryStr)
                    .stream().collect(Collectors.toMap(it -> Difference.Type.fromUri(it.get("type_join")), it -> ((Number) it.get("ns")).longValue(), (a, b) -> a, LinkedHashMap::new)));

            queryStr = prefix + "SELECT ?change ?type ?p1 ?p2 ?p3 FROM <" + changeset + "> WHERE {" + conditions + "} ORDER BY ?change OFFSET " + (query.getPage() * 20) + " LIMIT 20";
            response.setResults(querySelect(queryStr).stream().map(map -> new Difference(Difference.Type.fromUri(map.get("type")), map.get("p1"), map.get("p2"), map.get("p3"))).collect(Collectors.toList()));
        }

        response.setResolved(resolveURIs(oldVersion, newVersion, uris));
        return response;
    }

    private Map<String, DifferenceValue> resolveURIs(String oldVersion, String newVersion, Set<String> uris) throws IOException {
        Map<String, DifferenceValue> result = new HashMap<>();
        for (String uri : uris) {
            if (uri.startsWith("http://www.data-publica.com/lod/")) {
                // DP concepts
                if (uri.endsWith("-measure")) {
                    insertIfNotNull(result, uri, resolveURI(oldVersion, newVersion,
                            "<" + uri + "> rdfs:range ?range. OPTIONAL { <" + uri + "> diachron:codelist ?codelist}",
                            true));
                } else if (uri.endsWith("-concept")) {
                } else if (uri.endsWith("-dim")) {
                    insertIfNotNull(result, uri, resolveURI(oldVersion, newVersion,
                            "<" + uri + "> rdfs:range ?range. OPTIONAL { <" + uri + "> diachron:codelist ?codelist}",
                            true));
                } else if (uri.endsWith("-codelist")) {
                } else if (uri.endsWith("-obs")) {
                } else if (uri.contains("-value-")) {
                } else {
                    log.warn("Unknown uri to resolve <" + uri + ">... ignoring");
                }
            } else if (uri.startsWith("http://www.diachron-fp7.eu/resource/Record/")) {
                // Diachron Record
                if (uri.endsWith("-obs")) {

                } else {
                    log.warn("Unknown uri to resolve <" + uri + ">... ignoring");
                }
            } else {
                log.warn("Unknown uri to resolve <" + uri + ">... ignoring");
            }
        }
        return result;
    }

    private static void insertIfNotNull(Map<String, DifferenceValue> result, String uri, DifferenceValue obj) {
        if (obj == null) return;
        result.put(uri, obj);
    }

    private DifferenceValue<Map<String, Object>> resolveURI(String beforeGraph, String afterGraph, String query, boolean schemaset) throws IOException {
        if (schemaset) {
            beforeGraph = beforeGraph.replace("recordset", "schemaset");
            afterGraph = afterGraph.replace("recordset", "schemaset");
        } else {
            beforeGraph = beforeGraph.replace("schemaset", "recordset");
            afterGraph = afterGraph.replace("schemaset", "recordset");
        }
        String prefix = "PREFIX co: <http://www.diachron-fp7.eu/changes/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX diachron: <http://www.diachron-fp7.eu/resource/>\n" +
                "PREFIX qb: <http://purl.org/linked-data/cube#>\n";
        String queryStr = prefix + "SELECT * {\n" +
                "        { GRAPH <" + beforeGraph + "> {\n BIND (\"before\" as ?before)\n" +
                "            " + query + "\n" +
                "        }}\n" +
                "    UNION \n" +
                "        { GRAPH <" + afterGraph + "> {\n BIND (\"after\" as ?before)\n" +
                "            " + query + "\n" +
                "        }}\n" +
                "}\n" +
                "ORDER BY ?before";
        Map<String, Object> before = null;
        Map<String, Object> after = null;
        for (Map<String, Object> map : querySelect(queryStr)) {
            if (map.get("before").equals("before")) {
                before = map;
            } else {
                after = map;
            }
            map.remove("before");
        }
        if (before == null && after == null) return null;

        return new DifferenceValue(before, after);
    }

    public Map<String, Integer> getChangeSetStats(String datasetBaseURI, String newVersion, String oldVersion) throws IOException {
        String changeset = RESOURCE_BASE_URI + datasetBaseURI + "/changes/" + oldVersion.replaceFirst(".*/([^/]*)$", "$1") + "-" + newVersion.replaceFirst(".*/([^/]*)$", "$1");
        String query = "SELECT ?o (COUNT(?s) AS ?ns)\n" +
                "FROM <" + changeset + ">\n" +
                "WHERE {?s a ?o}\n" +
                "GROUP BY ?o\n" +
                "ORDER BY DESC(?ns)";
        return querySelect(query).stream().collect(Collectors.toMap(it -> it.get("o").toString().replace("http://www.diachron-fp7.eu/changes/", "").toUpperCase(), it -> (Integer) it.get("ns"), (a, b) -> a, LinkedHashMap::new));
    }


    private Model query(String query) throws IOException {
        HttpGet get = new HttpGet(config.getArchiveBaseUrl() + "/archive");
        HttpUriRequestUtil.setParams(get, ImmutableMap.of("query", query, "queryType", "CONSTRUCT"));
        return jsonResultsToModel(fetch(get));
    }

    private List<Map<String, Object>> querySelect(String query) throws IOException {
        HttpGet get = new HttpGet(config.getArchiveBaseUrl() + "/archive");
        HttpUriRequestUtil.setParams(get, ImmutableMap.of("query", query, "queryType", "SELECT"));
        return jsonResultsToMap(fetch(get));
    }

    private JsonNode fetch(HttpGet get) throws IOException {
        final JsonNode json = http.execute(get).json();
        if (!json.get("success").asBoolean()) {
            throw new IllegalStateException("Could not complete query: " + json.get("message").asText());
        }
        return json.get("data");
    }

    public Model getDatasetMetaData(String id, Long at, String restrictionId) throws IOException {
        return getDatasetMetaData(getFit(id, at).id, restrictionId);
    }

    public DatasetVersion getFit(String id, Long atTimestamp) throws IOException {
        final List<DatasetVersion> datasets = getDatasetVersions(id);
        final DatasetVersion latestEntry = datasets.get(0);
        if (atTimestamp == null)
            return latestEntry;
        Date at = new Date(atTimestamp);
        // If at is over the latest version, then take the latest version
        if (at.compareTo(latestEntry.date) >= 0)
            return latestEntry;
        final DatasetVersion firstEntry = datasets.get(datasets.size() - 1);

        // If at is below the first version, take the first version (could be an error)
        if (at.compareTo(firstEntry.date) <= 0)
            return firstEntry;

        // Else get the first entry that is below atee
        return datasets.stream().filter(it -> it.date.compareTo(at) < 0).findFirst().get();
    }

    public String serializeModel(Model model) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.write(out, "N3");
        return out.toString();
    }

    public String serializeModel(Model model, String format) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.write(out, format);
        return out.toString();
    }

    public ChangeSetResponse searchChanges(String name,
                                           boolean schema, // false: data, true: schema
                                           String fromVersion,
                                           String toVersion,
                                           Integer offset,
                                           Integer limit,
                                           Map<String, Object> filter) throws IOException {
        ChangeSetQuery query = new ChangeSetQuery();

        if (filter != null) {
            for (Map.Entry<String, Object> entry : filter.entrySet()) {
                if ("types".equals(entry.getKey())) {
                    query.setType(Difference.Type.valueOf(entry.getValue().toString()));
                } else if ("joinTypes".equals(entry.getKey())) {
                    query.setJoinType(Difference.Type.valueOf(entry.getValue().toString()));
                } else {
                    query.setProperty(entry.getKey(), entry.getValue());
                }
            }
        }

        String id = getDiachronicDSByName(name);

        List<DatasetVersion> versions = getDatasetVersions(id);
        if (fromVersion == null) {
            fromVersion = versions.get(0).recordSet;
        }
        if (toVersion == null) {
            toVersion = versions.get(versions.size() - 1).recordSet;
        }

        ChangeSetResponse response = getChangeSetResult((schema ? "schemaset/" : "recordset/") + name, fromVersion, toVersion, query);

        // processing excludes
        MultiMap<String, Object> excludes = schema ? schemaChangeExcludes : dataChangeExcludes;
        response.getFacets().getParameters().forEach((key, value) -> {
            List val = (List)excludes.get(key);
            if (val != null) {
                val.forEach(v -> value.remove(v));
            }
        });

        return response;
    }

    private MultiMap<String, Object> schemaChangeExcludes = new MultiValueMap<>();
    private MultiMap<String, Object> dataChangeExcludes = new MultiValueMap<>();

    public void hideChanges(String name, boolean schema, // false: data, true: schema
                            Map<String, Object> properties) throws IOException {

        MultiMap<String, Object> excludes = schema ? schemaChangeExcludes : dataChangeExcludes;
        excludes.putAll(properties);
        //todo add real implementation
        return;
    }

}
