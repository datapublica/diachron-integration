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


    @PostConstruct
    public void createFakeFacts() {
        versions[0] = new HashMap<>();
        versions[1] = new HashMap<>();

        addFact(versions[0], "flamingo", "pink", 500);
        addFact(versions[0], "flamingo", "white", 1000);
        addFact(versions[0], "bear", "brown", 2000);
        addFact(versions[0], "bear", "white", 5000);
        addFact(versions[0], "bear", "black", 1500);
        addFact(versions[0], "panther", "black", 3000);
        addFact(versions[0], "panther", "white", 35000);

        addFact(versions[1], "bear", "brown", 2000);
        addFact(versions[1], "bear", "white", 5000);
        addFact(versions[1], "bear", "black", 1500);
        addFact(versions[1], "panther", "black", 3000);
        addFact(versions[1], "panther", "white", 35000);
        addFact(versions[1], "starfish", "black", 300);
        addFact(versions[1], "starfish", "red", 100);

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