package com.datapublica.diachron.service;

import com.datapublica.common.http.DPHttpClient;
import com.datapublica.common.http.util.HttpUriRequestUtil;
import com.datapublica.diachron.config.DiachronConfig;
import com.datapublica.diachron.service.data.DatasetVersion;
import com.datapublica.diachron.util.StreamUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.hp.hpl.jena.rdf.model.*;
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
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class ArchiveService {
    private static final Logger log = LoggerFactory.getLogger(ArchiveService.class);

    public static final String DICTIONARY = "http://www.diachron-fp7.eu/archive/dictionary";

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
            uri += "-value-"+value;
        return uri;
    }

    /**
     *
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
                .addPart("DataFile", file)
                .build();

        post.setEntity(reqEntity);

        return http.execute(post).json().get("data").asText();
    }

    public List<DatasetVersion> getDatasetVersions(String dds) throws IOException {
        JsonNode results = queryTemplate("listDatasets", "diachronicDatasetId", dds).get("results").get("bindings");
        return StreamUtil.stream(results).map(it -> {
            String creationTime = it.get("creationTime").get("value").asText();
            String recordSet = it.get("recordSet").get("value").asText();
            String dataset = it.get("dataset").get("value").asText();

            return new DatasetVersion(dataset, recordSet, Date.from(ZonedDateTime.parse(creationTime).toInstant()));
        }).sorted((a, b) -> -a.date.compareTo(b.date)).collect(Collectors.toList());
    }


    public Model getDatasetMetaData(String datasetId, String subjectRestrictionURI) throws IOException {
        String queryString = "CONSTRUCT {?s ?p ?o} " +
                "WHERE {{" +
                "SELECT ?s ?p ?o FROM <" + datasetId +"> WHERE {?s ?p ?o}}}";
        if (subjectRestrictionURI != null) {
            queryString = queryString.replace("?s", "<" + subjectRestrictionURI+">");
        }
        return query(queryString, "CONSTRUCT");
    }

    public Model getDatasetData(String datasetId, String dimensionRestrictionURI) throws IOException {
        String queryString = "CONSTRUCT {?s ?p ?o} " +
                "WHERE {{" +
                "SELECT ?s ?p ?o FROM <" + RDFDictionary.getDictionaryNamedGraph() +"> " +
                "WHERE " +
                "{ <"+datasetId+"> <"+DiachronOntology.hasRecordSet+"> ?rs ." +
                "GRAPH ?rs {" +
                "{?rec <"+DiachronOntology.subject+"> ?s ; " +
                "<"+DiachronOntology.hasRecordAttribute+"> [<"+DiachronOntology.predicate+"> ?p ; <"+DiachronOntology.object+"> ?o]" +
                (dimensionRestrictionURI != null ?  " ; <"+DiachronOntology.hasRecordAttribute+"> [<"+DiachronOntology.object+"> <"+dimensionRestrictionURI+">]" : "") +
                "}" +
                " UNION {?s a <"+DiachronOntology.multidimensionalObservation+"> ; <"+DiachronOntology.hasRecordAttribute+"> [<"+DiachronOntology.predicate+"> ?p ; <"+DiachronOntology.object+"> ?o] "+(dimensionRestrictionURI != null ?  " ; <"+DiachronOntology.hasRecordAttribute+"> [<"+DiachronOntology.object+"> <"+dimensionRestrictionURI+">]" : "")+"}" +
                "}" +
                "}" +
                "}"+
                "}";
        return query(queryString, "CONSTRUCT");
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

    private JsonNode queryTemplate(String template, String... parameters) throws IOException {
        HttpGet get = new HttpGet(config.getArchiveBaseUrl() + "/archive/templates");
        final ImmutableMap<String, String> params;
        if (parameters.length == 2) {
            params = ImmutableMap.of("name", template, parameters[0], parameters[1]);
            log.info("Querying template "+template + " using " +parameters[0]+"="+parameters[1]);
        } else if (parameters.length == 4) {
            params = ImmutableMap.of("name", template, parameters[0], parameters[1], parameters[2], parameters[3]);
            log.info("Querying template "+template + " using " +parameters[0]+"="+parameters[1]+", "+parameters[2]+"="+parameters[3]);
        } else {
            throw new IllegalArgumentException("Illegal parameter size "+parameters.length);
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

    public Model getChangeSet(String newVersion, String oldVersion) throws IOException {
        String query = "CONSTRUCT {?s ?p ?o} " + // <http://www.diachron-fp7.eu/changes/new_version> <"+oldVersion+"> ;
                "WHERE {{" +"SELECT ?s ?p ?o FROM <http://www.diachron-fp7.eu/changes/multidimensional> WHERE {?s <http://www.diachron-fp7.eu/changes/old_version> <"+newVersion+"> ; ?p ?o\n" +
                "FILTER (?p != <http://www.diachron-fp7.eu/changes/old_version> && ?p != <http://www.diachron-fp7.eu/changes/new_version>)\n" +
                "}}}";
        return query(query, "CONSTRUCT");
    }

    private Model query(String query, String queryType) throws IOException {
        HttpGet get = new HttpGet(config.getArchiveBaseUrl() + "/archive");
        HttpUriRequestUtil.setParams(get, ImmutableMap.of("query", query, "queryType", queryType));
        return jsonResultsToModel(fetch(get));
    }

    private JsonNode fetch(HttpGet get) throws IOException {
        final JsonNode json = http.execute(get).json();
        if (!json.get("success").asBoolean()) {
            throw new IllegalStateException("Could not complete query: "+json.get("message").asText());
        }
        return json.get("data");
    }
}
