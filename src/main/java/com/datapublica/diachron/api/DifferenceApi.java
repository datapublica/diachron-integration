package com.datapublica.diachron.api;

import com.datapublica.diachron.service.ArchiveService;
import com.datapublica.diachron.service.data.Difference;
import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

/**
 * API for retrieving changes between versions of a dataset
 *
 * @author Jacques Belissent
 */
@Controller
@RequestMapping("/api/change")
public class DifferenceApi {


    @Autowired
    private ArchiveService service;

    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
    public Map get(@PathVariable String id,
                                      @RequestParam long fromVersion,
                                      @RequestParam long toVersion) throws IOException {
        /*
        final List<DatasetVersion> versions = service.getDatasetVersions(service.getDiachronicDSByName(id)).stream()
                .filter(it -> it.date.compareTo(new Date(fromVersion)) >= 0 && it.date.compareTo(new Date(toVersion)) <= 0)
                .collect(Collectors.toList());

        final Model changeSet = service.getChangeSet(versions.get(0).recordSet, versions.get(versions.size() - 1).recordSet);
*/
        // todo derive from actual change set instead
        MultiMap<Difference.Type, Difference> diffs = new MultiValueMap<>();

        diffs.put(Difference.Type.ATTACH_INSTANCE_TO_CODELIST, new Difference(Difference.Type.ATTACH_INSTANCE_TO_CODELIST, "animal", "starfish"));
        diffs.put(Difference.Type.ATTACH_INSTANCE_TO_CODELIST, new Difference(Difference.Type.ATTACH_INSTANCE_TO_CODELIST, "color", "red"));
        diffs.put(Difference.Type.DETACH_INSTANCE_FROM_CODELIST, new Difference(Difference.Type.DETACH_INSTANCE_FROM_CODELIST, "animal", "flamingo"));
        diffs.put(Difference.Type.DETACH_INSTANCE_FROM_CODELIST, new Difference(Difference.Type.DETACH_INSTANCE_FROM_CODELIST, "color", "pink"));
        diffs.put(Difference.Type.ATTACH_OBSERVATION_TO_FT, new Difference(Difference.Type.ATTACH_OBSERVATION_TO_FT, "animals", "starfish-black"));
        diffs.put(Difference.Type.ATTACH_OBSERVATION_TO_FT, new Difference(Difference.Type.ATTACH_OBSERVATION_TO_FT, "animals", "starfish-red"));
        diffs.put(Difference.Type.DETACH_DIMENSION_FROM_FT, new Difference(Difference.Type.DETACH_DIMENSION_FROM_FT, "animals", "flamingo-white"));
        diffs.put(Difference.Type.DETACH_DIMENSION_FROM_FT, new Difference(Difference.Type.DETACH_DIMENSION_FROM_FT, "animals", "flamingo-pink"));

        return diffs;
    }


}
