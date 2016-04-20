Kafka Graphite Metrics Reporter
==============================

[![Build Status](https://travis-ci.org/emetriq/kafka-graphite.svg)](https://travis-ci.org/emetriq/kafka-graphite)

This is a simple reporter for kafka using the 
[GraphiteReporter](https://dropwizard.github.io/metrics/2.2.0/apidocs/com/yammer/metrics/reporting/GraphiteReporter.html). It works with
kafka 0.8.x version.

Big thanks to Maxime Brugidou from Criteo who did the initial commit of the Ganglia version,
available here https://github.com/criteo/kafka-ganglia

Install On Broker
------------

1. Build the `kafka-graphite-1.0.2.jar` jar using `mvn package` or download it from the releases.
   Hint: The jar will include the metrics-graphite dependency
   which is not brought by Kafka.
2. Add `kafka-graphite-1.0.2.jar` to the `libs/` directory of your kafka broker installation
3. Configure the broker (see the configuration section below)
4. Restart the broker

Configuration
------------

Edit the `server.properties` file of your installation, activate the reporter by setting:

    kafka.metrics.reporters=com.criteo.kafka.KafkaGraphiteMetricsReporter
    kafka.graphite.metrics.reporter.enabled=true

You may also specify multiple comma-separated reporter classes for the `kafka.metrics.reporters` property:

    kafka.metrics.reporters=com.criteo.kafka.KafkaGraphiteMetricsReporter,kafka.metrics.KafkaCSVMetricsReporter[,....]

Here is a list of default properties used:

    kafka.graphite.metrics.host=localhost
    kafka.graphite.metrics.port=2003
    kafka.graphite.metrics.group=kafka
    # This can be use to exclude some metrics from graphite 
    # since kafka has quite a lot of metrics, it is useful
    # if you have many topics/partitions. For example :
    kafka.graphite.metrics.exclude.regex=(kafka.network.*|kafka.*.topic.*)

    # Each metric provides multiple dimensions: min, max, meanRate, etc
    # This might be too much data.
    # It is possible to disable some metric dimensions with the following properties:
    # By default all dimensions are enabled. Only the unwanted dimensions have to be configured
    kafka.graphite.dimension.enabled.count=false
    kafka.graphite.dimension.enabled.meanRate=false
    kafka.graphite.dimension.enabled.rate1m=false
    kafka.graphite.dimension.enabled.rate5m=false
    kafka.graphite.dimension.enabled.rate15m=false
    kafka.graphite.dimension.enabled.min=false
    kafka.graphite.dimension.enabled.max=false
    kafka.graphite.dimension.enabled.mean=false
    kafka.graphite.dimension.enabled.sum=false
    kafka.graphite.dimension.enabled.stddev=false
    kafka.graphite.dimension.enabled.median=false
    kafka.graphite.dimension.enabled.p75=false
    kafka.graphite.dimension.enabled.p95=false
    kafka.graphite.dimension.enabled.p98=false
    kafka.graphite.dimension.enabled.p99=false
    kafka.graphite.dimension.enabled.p999=false


Known Issues
----------

With Kafka  `<= 0.8.2.2` there is an issue if topics get deleted or partions are moved between brokers.
The metrics are not get deleted in this case and because they are implemented as a `Gauge`, a `NoSuchElementException`
is thrown when the metrics are reported.

There is already a fix for this, see [KAFKA-1866](https://issues.apache.org/jira/browse/KAFKA-1866) but it did not make
it into an 0.8.x release. Because of this we implemented a workaround for this within the `FilterMetricsPredicate`.
