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

import kafka.utils.VerifiableProperties;

import java.util.Properties;

public class KafkaGraphiteMetricsReporterTest {


    @Test
    public void initWithoutPropertiesSet() {
        KafkaGraphiteMetricsReporter reporter = new KafkaGraphiteMetricsReporter();
        reporter.init(new VerifiableProperties());
    }

    @Test
    public void initStartStopWithPropertiesSet() {
        KafkaGraphiteMetricsReporter reporter = new KafkaGraphiteMetricsReporter();
        Properties properties = new Properties();
        properties.setProperty("kafka.graphite.metrics.exclude.regex", "xyz.*");
        properties.setProperty("kafka.graphite.metrics.reporter.enabled", "true");

        reporter.init(new VerifiableProperties(properties));

        reporter.startReporter(1l);
        reporter.stopReporter();
    }


    @Test
    public void getMBeanName() {
        KafkaGraphiteMetricsReporter reporter = new KafkaGraphiteMetricsReporter();
        assertEquals("kafka:type=com.criteo.kafka.KafkaGraphiteMetricsReporter", reporter.getMBeanName());

    }
}
