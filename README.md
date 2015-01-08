Kafka Graphite Metrics Reporter
==============================

This is a simple reporter for kafka using the 
[GraphiteReporter](http://metrics.codahale.com/manual/graphite/). It works with 
kafka 0.8.1.1 version.

Big thanks to Maxime Brugidou from Criteo who did the initial commit of the Ganglia version,
available here https://github.com/criteo/kafka-ganglia

Install On Broker
------------

1. Build the `kafka-graphite-1.1.6.jar` jar using `mvn package`.
2. Add `kafka-graphite-1.1.6.jar` and `metrics-graphite-2.2.0.jar` to the `libs/` 
   directory of your kafka broker installation
3. Configure the broker (see the configuration section below)
4. Restart the broker

Configuration
------------

Edit the `server.properties` file of your installation, activate the reporter by setting:

    kafka.metrics.reporters=com.criteo.kafka.KafkaGraphiteMetricsReporter[,kafka.metrics.KafkaCSVMetricsReporter[,....]]
    kafka.graphite.metrics.reporter.enabled=true

Here is a list of default properties used:

    kafka.graphite.metrics.host=localhost
    kafka.graphite.metrics.port=8649
    kafka.graphite.metrics.group=kafka
    # This can be use to filter some metrics from graphite
    # since kafka has quite a lot of metrics, it is useful
    # if you have many topics/partitions.
    # Only metrics matching with the pattern will be written to graphite.
    kafka.graphite.metrics.filter.regex="kafka.server":type="BrokerTopicMetrics",.*

Usage As Lib
-----------

Simply build the jar and publish it to your maven internal repository (this
package is not published to any public repositories unfortunately).
