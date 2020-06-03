# C* Audit - Nats.io Trigger for Elassandra & Cassandra

Cassandra audit trigger built with scala sbt project for nats.io messaging. This is particularly useful to implement auditing prior to Cassandra 4, and useful for creating other boilerplate triggers.

## Getting Started with Scala SBT
* https://www.scala-sbt.org/1.x/docs/Setup.html

## Prepare TLS (Required)

Everything at SFPL uses encryption by default (even testing). You can find docker images with encryption on by default (with test certificates below).

From the ```.setup/keys``` directory:
```
keytool -keystore nats-truststore.jks -alias CARoot -import -file rootCa.crt -storepass password -noprompt -storetype pkcs12
cat nats-client.key nats-client.crt > combined.pem
openssl pkcs12 -export -in combined.pem -out cert.p12
keytool -importkeystore -srckeystore cert.p12 -srcstoretype pkcs12 -deststoretype pkcs12 -destkeystore nats-keystore.jks
keytool -keystore nats-keystore.jks -alias CARoot -import -file rootCa.crt -storepass password -noprompt
rm cert.p12 combined.pem
```

## Build & Deploy

Build jar file from this project(cstar-audit-nats-trigger.jar) and copy it to $CASSANDRA_HOME/triggers on **every cassandra server**, for example:

```
sbt assembly && \
sudo cp ./target/cstar-audit-nats-trigger-assembly-1.0.jar /etc/cassandra/triggers/
```
Then setup your config in ```/etc/cassandra/nats.conf```:
```
nats {
  servers = "nats://localhost:4222"
  servers = ${?NATS_SERVERS}
  defaultTopic = "tic.log.audit"
  defaultTopic = ${?NATS_DEFAULT_TOPIC}
  keystorePath = "/etc/cassandra/nats-keystore.jks"
  keystorePath = ${?NATS_KEYSTORE_PATH}
  keystorePassword = "password"
  keystorePassword = ${?NATS_KEYSTORE_PASSWORD}
  truststorePath = "/etc/cassandra/nats-truststore.jks"
  truststorePath = ${?NATS_TRUSTSTORE_PATH}
  truststorePassword = "password"
  truststorePassword = ${?NATS_TRUSTSTORE_PASSWORD}  
}
```

Then run something like:
```
CREATE TRIGGER auditblobs
        ON cstartest.blobs
        USING 'io.sfpl.cstar.AuditNatsTrigger';
```

Then run an auditable event (we always use [updater](https://github.com/sfproductlabs/tracker/blob/eafe7f90b1b740abe2a3a9324574491c27f5da99/.setup/schema.2.cql#L374) in our update queries so we know who's responsible). A simpler example:
```
INSERT INTO cstartest.blobs(id, payload) VALUES('001', 'dev');
```

And test:
```
cqlsh> SELECT * FROM cstartest.logs where params['__table'] = 'blobs' allow filtering;
```

### Dependencies

By default we run encryption for all of our test services, so you can find default setups for use with the configuration & keys above using the following dependencies.

#### Nats

Nats is required for this example. Install by running:

```
docker-compose up
```

Or get a copy from https://hub.docker.com/repository/docker/sfproductlabs/nats

#### Elassandra (Optional)

We recommend elassandra for your workhorse database over cassandra. It provides realtime querying (elastic integration) with cassandra scalability (https://elassandra.readthedocs.io/en/latest/installation.html). We have our own docker image for testing here: https://hub.docker.com/repository/docker/sfproductlabs/elassandra

#### Tracker (Optional)

We use an in house GDPR compliant telemetry system called [tracker](https://github.com/sfproductlabs/tracker). The schema for the default log entry, ends up in ```LOGS```` (https://github.com/sfproductlabs/tracker/blob/master/.setup/schema.2.cql).

Get a copy from https://hub.docker.com/repository/docker/sfproductlabs/tracker

## Credits

https://gitlab.com/rahasak-labs/siddhi/-/tree/master/siddhi-trigger
