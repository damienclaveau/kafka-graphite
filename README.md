Kafka Graphite Metrics Reporter
==============================

[![Build Status](https://travis-ci.org/emetriq/kafka-graphite.svg)](https://travis-ci.org/emetriq/kafka-graphite)

This is a simple reporter for kafka using the 
[GraphiteReporter](http://metrics.codahale.com/manual/graphite/). It works with 
kafka 0.8.x version.

Big thanks to Maxime Brugidou from Criteo who did the initial commit of the Ganglia version,
available here https://github.com/criteo/kafka-ganglia

Install On Broker
------------

1. Build the `kafka-graphite-1.0.0.jar` jar using `mvn package`.
   Hint: The jar will include the metrics-graphite dependency
   which is not brought by Kafka.
2. Add `kafka-graphite-1.0.0.jar` to the `libs/` directory of your kafka broker installation
3. Configure the broker (see the configuration section below)
4. Restart the broker

Configuration
------------

Edit the `server.properties` file of your installation, activate the reporter by setting:

    kafka.metrics.reporters=com.criteo.kafka.KafkaGraphiteMetricsReporter[,kafka.metrics.KafkaCSVMetricsReporter[,....]]
    kafka.graphite.metrics.reporter.enabled=true

Here is a list of default properties used:

    kafka.graphite.metrics.host=localhost
    kafka.graphite.metrics.port=2003
    kafka.graphite.metrics.group=kafka
    # This can be use to exclude some metrics from graphite 
    # since kafka has quite a lot of metrics, it is useful
    # if you have many topics/partitions.
    kafka.graphite.metrics.exclude.regex=<not set>

