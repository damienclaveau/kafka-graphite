package com.criteo.kafka;

import java.util.regex.Pattern;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;

/**
 * Predicate that excludes any metrics whose path matches the given regex
 */
public class RegexMetricExclusionPredicate implements MetricPredicate {

    Pattern pattern = null;

    public RegexMetricExclusionPredicate(String regex) {
        pattern = Pattern.compile(regex);
    }

    @Override
    public boolean matches(MetricName name, Metric metric) {
        boolean ok = !pattern.matcher(name.getName()).matches();
        return ok;
    }

}
