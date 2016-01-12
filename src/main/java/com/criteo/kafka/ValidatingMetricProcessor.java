/*
 *  Copyright 2015 emetriq GmbH
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

import java.util.NoSuchElementException;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.Timer;

/**
 *  Tries to get the value of a {@code Gauge} and wraps any {@code NoSuchElementException}
 *  into an {@code InvalidGaugeException}.
 *
 *  Reason for this is https://issues.apache.org/jira/browse/KAFKA-1866
 */
public class ValidatingMetricProcessor implements MetricProcessor {

    @Override
    public void processMeter(MetricName name, Metered meter, Object context) throws Exception {

    }

    @Override
    public void processCounter(MetricName name, Counter counter, Object context) throws Exception {

    }

    @Override
    public void processHistogram(MetricName name, Histogram histogram, Object context) throws Exception {

    }

    @Override
    public void processTimer(MetricName name, Timer timer, Object context) throws Exception {

    }

    @Override
    public void processGauge(MetricName name, Gauge gauge, Object context) throws Exception {
        try {
            gauge.value();
        } catch (NoSuchElementException ex) {
            throw new InvalidGaugeException(String.format("%s.%s.%s", name.getGroup(), name.getType(), name.getName()), ex);
        }
    }
}
