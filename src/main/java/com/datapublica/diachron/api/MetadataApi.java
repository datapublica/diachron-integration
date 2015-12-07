package com.datapublica.diachron.api;

import com.datapublica.diachron.service.data.Codelist;
import com.datapublica.diachron.service.data.Concept;
import com.datapublica.diachron.service.data.Dataset;
import com.datapublica.diachron.service.data.DatasetVersion;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Jacques Belissent
 */
@Controller
@RequestMapping("/api/meta")
public class MetadataApi {

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public Collection<Dataset> getDatasets() {
        List<Dataset> datasets = new ArrayList<>();

        Dataset animals = new Dataset();
        animals.setUri("animals");
        animals.setName("Animals");
        animals.setCreationDate(date(2015, 3, 3));

        Concept name = new Concept("name", "Name", Concept.BasicType.STRING);
        Codelist color = new Codelist("color", "Color", Concept.BasicType.STRING);
        color.setInstances("pink", "white", "brown", "black");
        Concept count = new Codelist("count", "Count", Concept.BasicType.INTEGER);

        DatasetVersion version = new DatasetVersion("0", null, date(2015, 3, 3));
        version.addDimension(name).addDimension(color).addMeasure(count);
        animals.addVersion(version);

        version = new DatasetVersion("1", null, date(2015, 9, 3));
        color = new Codelist("color", "Color", Concept.BasicType.STRING);
        color.setInstances("red", "white", "brown", "black");
        version.addDimension(name).addDimension(color).addMeasure(count);
        animals.addVersion(version);

        datasets.add(animals);
        return datasets;
    }

    private java.util.Date date(int year, int month, int dayOfMonth) {
        return Date.from(LocalDate.of(year, month, dayOfMonth).atStartOfDay().toInstant(ZoneOffset.UTC));
    }
}
