/*
 *  Copyright 2014 Damien Claveau
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */


package com.criteo.kafka;

import java.util.regex.Pattern;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;

/**
 * Implementation of {@link MetricPredicate} which will <b>exclude<b/> metrics if they match
 * the given regular expression.
 */
class RegexMetricPredicate implements MetricPredicate {

    private final Pattern pattern;

    /**
     * Constructor.
     *
     * @param regex the regular expression to match the metric names, can not be {@code null}
     */
    public RegexMetricPredicate(String regex) {
        pattern = Pattern.compile(regex);
    }

    @Override
    public boolean matches(MetricName name, Metric metric) {
        return !pattern.matcher(name.getName()).matches();
    }

}
