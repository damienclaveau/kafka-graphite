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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;

public class FilterMetricPredicateTest {

    private Metric metricMock;

    @Before
    public void setup() {
        metricMock = mock(Metric.class);

        // clean all metrics
        List<MetricName> metricNames = new ArrayList<MetricName>(Metrics.defaultRegistry().allMetrics().keySet());
        for (MetricName metricName : metricNames) {
            Metrics.defaultRegistry().removeMetric(metricName);
        }
    }

    @Test(expected = NullPointerException.class)
    public void constructorWithNull() {
         new FilterMetricPredicate(null);
    }

    @Test
    public void alwaysExcludeAppVersion_NoRegEx() {
        MetricPredicate predicate = new FilterMetricPredicate();

        assertFalse(predicate.matches(new MetricName("kafka.common", "AppInfo", "Version", null, "mBeanName"), metricMock));
        assertTrue(predicate.matches(new MetricName("kafka.common", "AppInfo", "SomethingElse", null, "mBeanName"), metricMock));
    }

    @Test
    public void alwaysExcludeAppVersion_WithRegEx() {
        MetricPredicate predicate = new FilterMetricPredicate("group.type.foobar.*");

        assertFalse(predicate.matches(new MetricName("kafka.common", "AppInfo", "Version", null, "mBeanName"), metricMock));
        assertTrue(predicate.matches(new MetricName("kafka.common", "AppInfo", "SomethingElse", null, "mBeanName"), metricMock));
     }

    @Test
    public void deleteGaugesIfTheyThrowNoSuchElementException() throws Exception {
        MetricPredicate predicate = new FilterMetricPredicate();

        MetricName metricNameToBeDeleted = new MetricName("test", "test", "delete", "scope", "mBeanName");

        Metric gaugeToBeDeleted = Metrics.newGauge(metricNameToBeDeleted, new Gauge<Long>() {
            @Override
            public Long value() {
                throw new NoSuchElementException("catch me if you can - i'm the the same as in KAFKA-1866");
            }
        });

        MetricName metricNameToStay = new MetricName("stay", "stay", "stay", "scope", "stay:mBeanName");
        Metric gaugeToStay = Metrics.newGauge(metricNameToStay, new Gauge<Long>() {
            @Override
            public Long value() {
                return 42L;
            }
        });


        assertFalse(predicate.matches(metricNameToBeDeleted, gaugeToBeDeleted));
        assertTrue(predicate.matches(metricNameToStay, gaugeToStay));


        assertFalse("The gauge should be deleted", Metrics.defaultRegistry().allMetrics().containsKey(metricNameToBeDeleted));
        assertTrue("The gauge should be there", Metrics.defaultRegistry().allMetrics().containsKey(metricNameToStay));
        assertEquals(Metrics.defaultRegistry().allMetrics().get(metricNameToStay), gaugeToStay);
    }

    @Test
    public void keepGaugesIfTheyThrowRuntimeExceptions() throws Exception {
        MetricPredicate predicate = new FilterMetricPredicate();

        MetricName metricName = new MetricName("test", "test", "delete", "scope", "mBeanName");

        Metric gauge = Metrics.newGauge(metricName, new Gauge<Long>() {
            @Override
            public Long value() {
                throw new RuntimeException("catch me if you can");
            }
        });

        assertTrue(predicate.matches(metricName, gauge));

        assertTrue("The gauge should be there", Metrics.defaultRegistry().allMetrics().containsKey(metricName));
        assertEquals(Metrics.defaultRegistry().allMetrics().get(metricName), gauge);
    }

    @Test
    public void matches() {
        MetricPredicate predicate = new FilterMetricPredicate("group.type.scope.foobar.*");

        assertFalse(predicate.matches(buildMetricName("foobar.count"), metricMock));
        assertFalse(predicate.matches(buildMetricName("foobar.rate"), metricMock));
        assertFalse(predicate.matches(buildMetricName("foobarbar"), metricMock));

        assertTrue(predicate.matches(buildMetricName("foo"), metricMock));
        assertTrue(predicate.matches(buildMetricName("bar"), metricMock));
        assertTrue(predicate.matches(buildMetricName("foo.bar"), metricMock));
    }

    private MetricName buildMetricName(String name) {
        return new MetricName("group", "type", name, "scope", "mBeanName");
    }

}
