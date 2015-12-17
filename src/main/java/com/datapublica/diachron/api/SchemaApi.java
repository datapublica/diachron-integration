package com.datapublica.diachron.api;

import com.datapublica.diachron.service.ArchiveService;
import com.datapublica.diachron.service.data.Dataset;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author Jacques Belissent
 */
@Controller
@RequestMapping("/api/meta")
public class SchemaApi {

    @Autowired
    private ArchiveService archive;

    private ObjectMapper jsonMapper = new ObjectMapper();

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public Collection<Dataset> getDatasets() throws IOException {
        return Collections.singletonList(getDataset("maires"));
    }

    @ResponseBody
    @RequestMapping(value = "/{name}", method = RequestMethod.GET, produces = "application/json")
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

    private String shortenId(String uri)  {
        return uri.replace(ArchiveService.RESOURCE_BASE_URI, "")
                .replaceAll("\\/", ":");
    }

    private Object getModel(String id) throws IOException {
        Model model = archive.getDatasetMetaData(id, null);
        String json = archive.serializeModel(model, "RDF/JSON");
        return jsonMapper.readValue(json, Object.class);
    }

}
