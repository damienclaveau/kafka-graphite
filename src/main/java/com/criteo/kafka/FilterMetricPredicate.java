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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricProcessor;

/**
 * Implementation of {@link MetricPredicate} which will <b>exclude<b/> metrics if they match
 * the given regular expression.<br>
 * It will also exclude the {@code kafka.common.AppInfo.Version} metric which causes warnings in graphite because of the invalid value.
 * And it will exclude and remove all Gauges which are no longer available on the broker.
 */
class FilterMetricPredicate implements MetricPredicate {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterMetricPredicate.class);

    private static final MetricProcessor metricProcessor = new ValidatingMetricProcessor();

    private static final Pattern APPVERSION_PATTERN = Pattern.compile("kafka.common.AppInfo.Version");

    private final Pattern pattern;

    /**
     * Default constructor which just excludes the metric containing Kafka's version
     */
    public FilterMetricPredicate() {
        pattern = null;
    }

    /**
     * Constructor.
     *
     * @param regex the regular expression to match the metric names, can not be {@code null}
     */
    public FilterMetricPredicate(String regex) {
        pattern = Pattern.compile(regex);
    }

    @Override
    public boolean matches(MetricName name, Metric metric) {
        String metricName = sanitizeName(name);

        boolean isVersionMetric = APPVERSION_PATTERN.matcher(metricName).matches();

        if (isVersionMetric || cleanInvalidGauge(name, metric, metricName)) {
            return false;
        }

        if (pattern != null) {
            return !pattern.matcher(metricName).matches();
        }

        return true;
    }

    /**
     * Filter gauges that should have been deleted, ugly workaround for KAFKA-1866 with Kafka 0.8.x
     * @return {@code false} if gauge is not cleaned.
     */
    private boolean cleanInvalidGauge(MetricName name, Metric metric, String metricName) {
        try {
            metric.processWith(metricProcessor, name, null);
        } catch (InvalidGaugeException ex) {
            LOGGER.info("Deleting metric {} from registry", metricName);
            Metrics.defaultRegistry().removeMetric(name);
            return true;
        } catch (Exception ex) {
            LOGGER.error("Caught an Exception while processing metric " + metricName, ex);
        }
        return false;
    }


    // same as in GraphiteReporter
    // the scope is needed to support for example the metrics like: kafka.server.BrokerTopicMetrics.topic.TOPICNAME.BytesOutPerSec
    private String sanitizeName(MetricName name) {
        final StringBuilder sb = new StringBuilder()
                .append(name.getGroup())
                .append('.')
                .append(name.getType())
                .append('.');
        if (name.hasScope()) {
            sb.append(name.getScope())
                    .append('.');
        }
        return sb.append(name.getName()).toString();
    }

}
