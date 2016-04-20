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


import com.yammer.metrics.core.*;
import com.yammer.metrics.stats.Snapshot;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.reporting.SocketProvider;
import com.yammer.metrics.reporting.GraphiteReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.EnumSet;

import static com.criteo.kafka.Dimension.*;


/**
 * A simple reporter which sends out application metrics to a <a href="http://graphite.wikidot.com/faq">Graphite</a>
 * server periodically.
 * Metrics dimensions are filtered in the overriden methods
 */
public class FilteredGraphiteReporter extends GraphiteReporter {
    
    private final EnumSet<Dimension> dimensions;
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaGraphiteMetricsReporter.class);

    /**
     * Creates a new {@link GraphiteReporter}.
     *
     * @param metricsRegistry the metrics registry
     * @param prefix          is prepended to all names reported to graphite
     * @param predicate       filters metrics to be reported
     * @param dimensions      enum of enabled dimensions to include
     * @param socketProvider  a {@link SocketProvider} instance
     * @param clock           a {@link Clock} instance
     * @throws IOException if there is an error connecting to the Graphite server
     */
    public FilteredGraphiteReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate, EnumSet<Dimension> dimensions, SocketProvider socketProvider, Clock clock) throws IOException {    
        super(metricsRegistry, prefix, predicate, socketProvider, clock);
        this.dimensions = dimensions;
        LOGGER.debug("The following Metrics Dimensions will be sent {}", dimensions);
    }
    
    protected static final Dimension[] meterDims = {count, meanRate, rate1m, rate5m, rate15m};
    protected static final Dimension[] summarizableDims = {min, max, mean, stddev, sum};
    protected static final Dimension[] SamplingDims = {median, p75, p95, p98, p99, p999};

    @Override
    public void processMeter(MetricName name, Metered meter, Long epoch) throws IOException {
        final String sanitizedName = sanitizeName(name);
        if (dimensions.contains(count))
            sendInt(epoch, sanitizedName, "count", meter.count());
        if (dimensions.contains(meanRate))
            sendFloat(epoch, sanitizedName, "meanRate", meter.meanRate());
        if (dimensions.contains(rate1m))
            sendFloat(epoch, sanitizedName, "1MinuteRate", meter.oneMinuteRate());
        if (dimensions.contains(rate5m))
            sendFloat(epoch, sanitizedName, "5MinuteRate", meter.fiveMinuteRate());
        if (dimensions.contains(rate15m))
            sendFloat(epoch, sanitizedName, "15MinuteRate", meter.fifteenMinuteRate());
    }

    @Override
    protected void sendSummarizable(long epoch, String sanitizedName, Summarizable metric) throws IOException {
        if (dimensions.contains(min))
            sendFloat(epoch, sanitizedName, "min", metric.min());
        if (dimensions.contains(max))
            sendFloat(epoch, sanitizedName, "max", metric.max());
        if (dimensions.contains(mean))
            sendFloat(epoch, sanitizedName, "mean", metric.mean());
        if (dimensions.contains(stddev))
            sendFloat(epoch, sanitizedName, "stddev", metric.stdDev());
        if (dimensions.contains(sum))
            sendFloat(epoch, sanitizedName, "sum", metric.sum());
    }

    @Override
    protected void sendSampling(long epoch, String sanitizedName, Sampling metric) throws IOException {
        final Snapshot snapshot = metric.getSnapshot();
        if (dimensions.contains(median))
            sendFloat(epoch, sanitizedName, "median", snapshot.getMedian());
        if (dimensions.contains(p75))
            sendFloat(epoch, sanitizedName, "75percentile", snapshot.get75thPercentile());
        if (dimensions.contains(p95))
            sendFloat(epoch, sanitizedName, "95percentile", snapshot.get95thPercentile());
        if (dimensions.contains(p98))
            sendFloat(epoch, sanitizedName, "98percentile", snapshot.get98thPercentile());
        if (dimensions.contains(p99))
            sendFloat(epoch, sanitizedName, "99percentile", snapshot.get99thPercentile());
        if (dimensions.contains(p999))
            sendFloat(epoch, sanitizedName, "999percentile", snapshot.get999thPercentile());
    }

}
