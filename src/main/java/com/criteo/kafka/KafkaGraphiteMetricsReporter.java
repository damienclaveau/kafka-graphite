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

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricPredicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import kafka.metrics.KafkaMetricsConfig;
import kafka.metrics.KafkaMetricsReporter;
import kafka.metrics.KafkaYammerMetrics;
import kafka.utils.VerifiableProperties;

public class KafkaGraphiteMetricsReporter implements KafkaMetricsReporter, KafkaGraphiteMetricsReporterMBean {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaGraphiteMetricsReporter.class);

    private static final String GRAPHITE_DEFAULT_HOST = "localhost";
    private static final int GRAPHITE_DEFAULT_PORT = 2003;
    private static final String GRAPHITE_DEFAULT_PREFIX = "kafka";

    private boolean initialized = false;
    private boolean running = false;
    private FilteredGraphiteReporter reporter = null;
    private String graphiteHost = GRAPHITE_DEFAULT_HOST;
    private int graphitePort = GRAPHITE_DEFAULT_PORT;
    private String metricPrefix = GRAPHITE_DEFAULT_PREFIX;
    private MetricPredicate metricPredicate = new FilterMetricPredicate();
    private EnumSet<Dimension> metricDimensions;

    @Override
    public String getMBeanName() {
        return "kafka:type=" + KafkaGraphiteMetricsReporter.class.getName();
    }

    @Override
    public synchronized void startReporter(long pollingPeriodSecs) {
        if (initialized && !running) {
            reporter.start(pollingPeriodSecs, TimeUnit.SECONDS);
            running = true;
            LOG.info("Started Kafka Graphite metrics reporter with polling period {} seconds", pollingPeriodSecs);
        }
    }

    @Override
    public synchronized void stopReporter() {
        if (initialized && running) {
            reporter.shutdown();
            running = false;
            LOG.info("Stopped Kafka Graphite metrics reporter");
            reporter = buildGraphiteReporter();
        }
    }

    @Override
    public synchronized void init(VerifiableProperties props) {
        if (!initialized) {
            KafkaMetricsConfig metricsConfig = new KafkaMetricsConfig(props);
            graphiteHost = props.getString("kafka.graphite.metrics.host", GRAPHITE_DEFAULT_HOST);
            graphitePort = props.getInt("kafka.graphite.metrics.port", GRAPHITE_DEFAULT_PORT);
            metricPrefix = props.getString("kafka.graphite.metrics.group", GRAPHITE_DEFAULT_PREFIX);
            String excludeRegex = props.getString("kafka.graphite.metrics.exclude.regex", null);
            metricDimensions = Dimension.fromProperties(props.props(), "kafka.graphite.dimension.enabled.");
    
            LOG.debug("Initialize GraphiteReporter [{},{},{}]", graphiteHost, graphitePort, metricPrefix);

            if (excludeRegex != null) {
                LOG.debug("Using regex [{}] for GraphiteReporter", excludeRegex);
                metricPredicate = new FilterMetricPredicate(excludeRegex);
            }
            reporter = buildGraphiteReporter();

            if (props.getBoolean("kafka.graphite.metrics.reporter.enabled", false)) {
                initialized = true;
                startReporter(metricsConfig.pollingIntervalSecs());
                LOG.debug("GraphiteReporter started.");
            }
        }
    }

    //Metrics was deprecated in 2.6 and it was replaced with KafkaYammerMetrics
    private FilteredGraphiteReporter buildGraphiteReporter() {
        FilteredGraphiteReporter graphiteReporter = null;
        try {
            graphiteReporter = new FilteredGraphiteReporter(
                    KafkaYammerMetrics.defaultRegistry(),
                    metricPrefix,
                    metricPredicate,
                    metricDimensions,
                    new FilteredGraphiteReporter.DefaultSocketProvider(graphiteHost, graphitePort),
                    Clock.defaultClock()
            );
        } catch (IOException e) {
            LOG.error("Unable to initialize GraphiteReporter", e);
        }
        return graphiteReporter;
    }
}
