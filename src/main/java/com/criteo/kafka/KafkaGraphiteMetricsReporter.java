package com.criteo.kafka;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import kafka.metrics.KafkaMetricsConfig;
import kafka.metrics.KafkaMetricsReporter;
import kafka.utils.VerifiableProperties;

import org.apache.log4j.Logger;

import com.criteo.kafka.KafkaGraphiteMetricsReporterMBean;
import com.criteo.kafka.RegexMetricPredicate;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.reporting.GraphiteReporter;
import com.yammer.metrics.reporting.GraphiteReporter.DefaultSocketProvider;

public class KafkaGraphiteMetricsReporter implements KafkaMetricsReporter,
	KafkaGraphiteMetricsReporterMBean {

	static Logger LOG = Logger.getLogger(KafkaGraphiteMetricsReporter.class);
	static String GRAPHITE_DEFAULT_HOST = "localhost";
	static int GRAPHITE_DEFAULT_PORT = 2003;
	static String GRAPHITE_DEFAULT_PREFIX = "kafka";
	
	boolean initialized = false;
	boolean running = false;
	GraphiteReporter reporter = null;
    String graphiteHost = GRAPHITE_DEFAULT_HOST;
    int graphitePort = GRAPHITE_DEFAULT_PORT;
    String graphiteGroupPrefix = GRAPHITE_DEFAULT_PREFIX;
    MetricPredicate predicate = MetricPredicate.ALL;
    private ScheduledExecutorService executor;

	@Override
	public String getMBeanName() {
		return "kafka:type=com.criteo.kafka.KafkaGraphiteMetricsReporter";
	}

	@Override
	public synchronized void startReporter(long pollingPeriodSecs) {
		if (initialized && !running) {
			executor.scheduleAtFixedRate(reporter, pollingPeriodSecs, pollingPeriodSecs, TimeUnit.SECONDS);
			running = true;
			LOG.info(String.format("Started Kafka Graphite metrics reporter with polling period %d seconds", pollingPeriodSecs));
		}
	}

	@Override
	public synchronized void stopReporter() {
		if (initialized && running) {
			executor.shutdown();
			running = false;
			LOG.info("Stopped Kafka Graphite metrics reporter");
            try {
            	reporter = new GraphiteReporter(
            			Metrics.defaultRegistry(),
            			graphiteGroupPrefix,
            			predicate,
            			new DefaultSocketProvider(graphiteHost, graphitePort),
            			Clock.defaultClock());
            } catch (IOException e) {
            	LOG.error("Unable to initialize GraphiteReporter", e);
            }
		}
	}

	@Override
	public synchronized void init(VerifiableProperties props) {
		if (!initialized) {
			KafkaMetricsConfig metricsConfig = new KafkaMetricsConfig(props);
            graphiteHost = props.getString("kafka.graphite.metrics.host", GRAPHITE_DEFAULT_HOST);
            graphitePort = props.getInt("kafka.graphite.metrics.port", GRAPHITE_DEFAULT_PORT);
            graphiteGroupPrefix = props.getString("kafka.graphite.metrics.group", GRAPHITE_DEFAULT_PREFIX);
            String regex = props.getString("kafka.graphite.metrics.filter.regex", null);

            LOG.debug("Initialize GraphiteReporter ["+graphiteHost+","+graphitePort+","+graphiteGroupPrefix+"]");

            executor = Metrics.defaultRegistry().newScheduledThreadPool(1, "MetricReporter");

            if (regex != null)
            	predicate = new RegexMetricPredicate(regex);
            else
            	predicate = MetricPredicate.ALL;
            
            try {
            	reporter = new GraphiteReporter(
            			Metrics.defaultRegistry(),
            			graphiteGroupPrefix,
            			predicate,
            			new DefaultSocketProvider(graphiteHost, graphitePort),
            			Clock.defaultClock());
            } catch (IOException e) {
            	LOG.error("Unable to initialize GraphiteReporter", e);
            }
            if (props.getBoolean("kafka.graphite.metrics.reporter.enabled", false)) {
            	initialized = true;
            	startReporter(metricsConfig.pollingIntervalSecs());
                LOG.debug("GraphiteReporter started.");
            }
        }
	}
}
