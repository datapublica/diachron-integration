package com.datapublica.diachron.api;

import com.datapublica.diachron.service.data.Difference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
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


    @PostConstruct
    public void createFakeFacts() {
        versions[0] = new HashMap<>();
        versions[1] = new HashMap<>();

        addFact(versions[0], "flamingo", "pink", 12);
        addFact(versions[0], "flamingo", "white", 10);
        addFact(versions[0], "bear", "brown", 6);
        addFact(versions[0], "bear", "white", 2);
        addFact(versions[0], "bear", "black", 11);
        addFact(versions[0], "panther", "black", 3);
        addFact(versions[0], "panther", "white", 1);

        addFact(versions[0], "bear", "brown", 6);
        addFact(versions[0], "bear", "white", 2);
        addFact(versions[0], "bear", "black", 10);
        addFact(versions[0], "panther", "black", 3);
        addFact(versions[0], "panther", "white", 1);
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
}