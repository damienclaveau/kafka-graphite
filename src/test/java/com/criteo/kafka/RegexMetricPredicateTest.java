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

import static org.junit.Assert.*;
import org.junit.Test;

import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;

public class RegexMetricPredicateTest {

    @Test(expected = NullPointerException.class)
    public void constructorWithNull() {
         new RegexMetricPredicate(null);
    }


    @Test
    public void matches() {
        MetricPredicate predicate = new RegexMetricPredicate("foobar.*");

        assertFalse(predicate.matches(buildMetricName("foobar.count"), null));
        assertFalse(predicate.matches(buildMetricName("foobar.rate"), null));
        assertFalse(predicate.matches(buildMetricName("foobarbar"), null));

        assertTrue(predicate.matches(buildMetricName("foo"), null));
        assertTrue(predicate.matches(buildMetricName("bar"), null));
        assertTrue(predicate.matches(buildMetricName("foo.bar"), null));
    }






    private MetricName buildMetricName(String name) {
        return new  MetricName("group", "type", name, "scope", "mBeanName");
    }
}
