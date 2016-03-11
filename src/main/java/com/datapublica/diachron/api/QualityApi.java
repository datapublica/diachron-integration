package com.datapublica.diachron.api;

import com.datapublica.diachron.service.QualityService;
import com.datapublica.diachron.service.data.DiscreteDistribution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Jacques Belissent
 */
@Controller
@RequestMapping("/api/quality")
public class QualityApi {

    @Autowired
    QualityService service;

    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = "application/turtle")
    List<Map<String, Object>> save(@RequestBody byte[] body, @PathVariable String id) throws Exception {
        String uri = "http://www.data-publica.com/lod-test/"+id;
        service.computeQualityMetrics(uri, body);
        return service.getQuality(uri);
    }


    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    List<Map<String, Object>> save(@PathVariable String id) throws Exception {
        String uri = "http://www.data-publica.com/lod/publication/"+id;
        return service.getQuality(uri);
    }

    public Map<String, DiscreteDistribution> getValueDistributions() {
        // create fake result todo use the quality service
        return null;
    }
}
