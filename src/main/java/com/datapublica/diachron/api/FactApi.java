package com.datapublica.diachron.api;

import com.datapublica.diachron.service.data.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

/**
 * fact table data access
 *
 * @author Jacques Belissent
 */
@Controller
@RequestMapping("/api/data")
public class FactApi {

    Map<String, List>[] versions = new Map[2];
    List<Difference> changes = new ArrayList<>();
    private Dataset animals;

    @PostConstruct
    public void createFakeFacts() {
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

        versions[0] = new HashMap<>();
        versions[1] = new HashMap<>();

        addFact(versions[0], "flamingo", "pink", 12);
        addFact(versions[0], "flamingo", "white", 10);
        addFact(versions[0], "bear", "brown", 6);
        addFact(versions[0], "bear", "white", 2);
        addFact(versions[0], "bear", "black", 11);
        addFact(versions[0], "panther", "black", 3);
        addFact(versions[0], "panther", "white", 1);
        addFact(versions[0], "sheep", "black", null);
        addFact(versions[0], "sheep", "white", null);

        addFact(versions[0], "bear", "brown", 6);
        addFact(versions[0], "bear", "white", null);
        addFact(versions[0], "bear", "black", 10);
        addFact(versions[0], "panther", "black", 3);
        addFact(versions[0], "panther", "white", 1);
        addFact(versions[0], "sheep", "black", 11);
        addFact(versions[0], "sheep", "white", 11);
        addFact(versions[1], "starfish", "black", 3);
        addFact(versions[1], "starfish", "red", 10);

        changes.add(new Difference(Difference.Type.ATTACH_INSTANCE_TO_CODELIST, "animal", "starfish"));
        changes.add(new Difference(Difference.Type.ATTACH_INSTANCE_TO_CODELIST, "color", "red"));
        changes.add(new Difference(Difference.Type.DETACH_INSTANCE_FROM_CODELIST, "animal", "flamingo"));
        changes.add(new Difference(Difference.Type.DETACH_INSTANCE_FROM_CODELIST, "color", "pink"));
        changes.add(new Difference(Difference.Type.ATTACH_OBSERVATION_TO_FT, "animals", "starfish-black"));
        changes.add(new Difference(Difference.Type.ATTACH_OBSERVATION_TO_FT, "animals", "starfish-red"));
        changes.add(new Difference(Difference.Type.DETACH_OBSERVATION_FROM_FT, "animals", "flamingo-white"));
        changes.add(new Difference(Difference.Type.DETACH_OBSERVATION_FROM_FT, "animals", "flamingo-pink"));

        changes.add(new Difference(Difference.Type.DELETE_MEASURE_VALUE_FROM_OBSERVATION, "bear-white", "count"));
        changes.add(new Difference(Difference.Type.ADD_MEASURE_VALUE_TO_OBSERVATION, "sheep-black", "count"));
        changes.add(new Difference(Difference.Type.ADD_MEASURE_VALUE_TO_OBSERVATION, "sheep-white", "count"));
        changes.add(new Difference(Difference.Type.ADD_OBSERVATION, "starfish-black"));
        changes.add(new Difference(Difference.Type.ADD_OBSERVATION, "starfish-red"));
        changes.add(new Difference(Difference.Type.DELETE_OBSERVATION, "flamingo-pink"));
        changes.add(new Difference(Difference.Type.DELETE_OBSERVATION, "flamingo-white"));
                //changes.add(new Difference(Difference.Type.ADD_UNKNOWN_PROPERTY: 17941,
//                changes.add(new Difference(Difference.Type.ADD_GENERIC_VALUE_TO_OBSERVATION: 16682,
        //changes.add(new Difference(Difference.Type.DELETE_UNKNOWN_PROPERTY: 31442,
        //changes.add(new Difference(Difference.Type.DELETE_GENERIC_VALUE_FROM_OBSERVATION: 30679,
    }

    void addFact(Map<String, List> table, Object... values) {
        table.put(values[0] + ":" + values[1], Arrays.asList(values));
    }

    @ResponseBody
    @RequestMapping(value = "/{datasetId}/{version}/{factTableId}/{factId}", method = RequestMethod.GET, produces = "text/plain")
    public Collection get(@PathVariable String datasetId, @PathVariable Integer version, @PathVariable String factTableId, @PathVariable String factId) throws IOException {
        return versions[version].values();
    }

    @ResponseBody
    @RequestMapping(value = "/{datasetId}/{version}/{factTableId}/{factId}/{column}", method = RequestMethod.GET, produces = "text/plain")
    public Collection aggregateCount(@PathVariable String datasetId, @PathVariable Integer version, @PathVariable String factTableId, @PathVariable String factId, @PathVariable String column) throws IOException {
        return versions[version].values();
    }

    private java.util.Date date(int year, int month, int dayOfMonth) {
        return Date.from(LocalDate.of(year, month, dayOfMonth).atStartOfDay().toInstant(ZoneOffset.UTC));
    }
}