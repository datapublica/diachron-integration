package com.datapublica.diachron.service.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * describes the value distribution for a categorical variable
 *
 * For instance, imagine a gender column in a data set, containing 3 values M, F, or "none",
 *
 * @author Jacques Belissent
 */
public interface DiscreteDistribution<KEY_TYPE> extends Map<KEY_TYPE, Long> {
}
