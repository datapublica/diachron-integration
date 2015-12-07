package com.datapublica.diachron.api;

import com.datapublica.diachron.service.data.DiscreteDistribution;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @author Jacques Belissent
 */
@Controller
@RequestMapping("/api/quality")
public class QualityApi {

    public Map<String, DiscreteDistribution> getValueDistributions() {
        // create fake result todo use the quality service
return null;
    }
}
