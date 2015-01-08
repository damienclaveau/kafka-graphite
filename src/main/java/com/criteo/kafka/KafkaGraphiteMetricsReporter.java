package com.criteo.kafka;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import kafka.metrics.KafkaMetricsConfig;
import kafka.metrics.KafkaMetricsReporter;
import kafka.utils.VerifiableProperties;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricPredicate;
import org.apache.log4j.*;

public class KafkaGraphiteMetricsReporter implements KafkaMetricsReporter,
	KafkaGraphiteMetricsReporterMBean {

	static Logger LOG = Logger.getLogger(KafkaGraphiteMetricsReporter.class);
	static String GRAPHITE_DEFAULT_HOST = "localhost";
	static int GRAPHITE_DEFAULT_PORT = 2003;
	static String GRAPHITE_DEFAULT_PREFIX = "kafka";
	static boolean HIDE_METER_MEANS_DEFAULT = false;  // Export all metrics by default

	String graphiteHost = GRAPHITE_DEFAULT_HOST;
	int graphitePort = GRAPHITE_DEFAULT_PORT;
	String graphiteGroupPrefix = GRAPHITE_DEFAULT_PREFIX;
	boolean hideMetersMeans = HIDE_METER_MEANS_DEFAULT;

	GraphiteReporter reporter = null;
    MetricPredicate predicate = MetricPredicate.ALL;
    private ScheduledExecutorService executor;

	boolean initialized = false;
	boolean running = false;

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
			reporter = createReporter(graphiteHost, graphitePort, graphiteGroupPrefix, predicate, hideMetersMeans);
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
			String logDir = props.getString("kafka.graphite.metrics.logFile", null);
			hideMetersMeans = props.getBoolean("kafka.graphite.metrics.hideMetersMeans", false);

			if (logDir != null) {
				setLogFile(logDir);
			}

            LOG.debug("Initialize GraphiteReporter ["+graphiteHost+","+graphitePort+","+graphiteGroupPrefix+"]");

            executor = Metrics.defaultRegistry().newScheduledThreadPool(1, "MetricReporter");

            if (regex != null)
            	predicate = new RegexMetricPredicate(regex);
            else
            	predicate = MetricPredicate.ALL;

			reporter = createReporter(graphiteHost, graphitePort, graphiteGroupPrefix, predicate, hideMetersMeans);

            if (props.getBoolean("kafka.graphite.metrics.reporter.enabled", false)) {
            	initialized = true;
            	startReporter(metricsConfig.pollingIntervalSecs());
                LOG.debug("GraphiteReporter started.");
            }
        }
	}

	private GraphiteReporter createReporter(String graphiteHost, int graphitePort, String graphiteGroupPrefix,
											MetricPredicate predicate, boolean hideMetersMeans){
		GraphiteReporter reporter = null;

		try {
			reporter = new GraphiteReporter(
					Metrics.defaultRegistry(),
					graphiteGroupPrefix,
					predicate,
					new GraphiteReporter.DefaultSocketProvider(graphiteHost, graphitePort),
					Clock.defaultClock());
			reporter.setHideMetersMeans(hideMetersMeans);
		} catch (IOException e) {
			LOG.error("Unable to initialize GraphiteReporter", e);
		}

		return reporter;
	}

	private void setLogFile(String logDir){
		RollingFileAppender fa = new RollingFileAppender();
		fa.setName("GraphiteReporter");
		fa.setFile(logDir);
		fa.setLayout(new PatternLayout("[%d] %-4r [%t] %-5p %c %x - %m%n"));
		fa.setThreshold(Level.INFO);
		fa.setMaxFileSize("256MB");
		fa.setMaxBackupIndex(2);
		fa.setAppend(true);
		fa.activateOptions();

		LogManager.getLogger("com.criteo").removeAllAppenders();
		LogManager.getLogger("com.criteo").addAppender(fa);
	}
}
