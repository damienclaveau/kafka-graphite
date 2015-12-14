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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.reporting.GraphiteReporter;

import kafka.metrics.KafkaMetricsConfig;
import kafka.metrics.KafkaMetricsReporter;
import kafka.utils.VerifiableProperties;

public class KafkaGraphiteMetricsReporter implements KafkaMetricsReporter, KafkaGraphiteMetricsReporterMBean {

	private static final Logger LOG = LoggerFactory.getLogger(KafkaGraphiteMetricsReporter.class);

	private static final String GRAPHITE_DEFAULT_HOST = "localhost";
	private static final int GRAPHITE_DEFAULT_PORT = 2003;
	private static final String GRAPHITE_DEFAULT_PREFIX = "kafka";
	
	private boolean initialized = false;
	private boolean running = false;
	private GraphiteReporter reporter = null;
    private String graphiteHost = GRAPHITE_DEFAULT_HOST;
    private int graphitePort = GRAPHITE_DEFAULT_PORT;
    private String graphiteGroupPrefix = GRAPHITE_DEFAULT_PREFIX;
    private MetricPredicate predicate = MetricPredicate.ALL;

	@Override
	public String getMBeanName() {
		return "kafka:type=com.criteo.kafka.KafkaGraphiteMetricsReporter";
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
            graphiteGroupPrefix = props.getString("kafka.graphite.metrics.group", GRAPHITE_DEFAULT_PREFIX);
            String regex = props.getString("kafka.graphite.metrics.exclude.regex", null);

            LOG.debug("Initialize GraphiteReporter [{},{},{}]", graphiteHost, graphitePort, graphiteGroupPrefix);

            if (regex != null) {
				LOG.debug("Using regex [{}] for GraphiteReporter", regex);
            	predicate = new RegexMetricPredicate(regex);
            }
			reporter = buildGraphiteReporter();

            if (props.getBoolean("kafka.graphite.metrics.reporter.enabled", false)) {
            	initialized = true;
            	startReporter(metricsConfig.pollingIntervalSecs());
                LOG.debug("GraphiteReporter started.");
            }
        }
	}


	private GraphiteReporter buildGraphiteReporter() {
		GraphiteReporter graphiteReporter = null;
		try {
			graphiteReporter = new GraphiteReporter(
					Metrics.defaultRegistry(),
					graphiteGroupPrefix,
					predicate,
					new GraphiteReporter.DefaultSocketProvider(graphiteHost, graphitePort),
					Clock.defaultClock()
			);
		} catch (IOException e) {
			LOG.error("Unable to initialize GraphiteReporter", e);
		}
		return graphiteReporter;
	}
}
