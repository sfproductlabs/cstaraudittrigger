# C* Audit - Nats.io Trigger for Elassandra & Cassandra

Cassandra audit trigger built with scala sbt project for nats.io messaging. This is particularly useful to implement auditing prior to Cassandra 4, and useful for creating other boilerplate triggers.

## Prepare TLS

From the ```.setup/keys``` directory:
```
keytool -keystore nats-truststore.jks -alias CARoot -import -file rootCa.crt -storepass password -noprompt -storetype pkcs12
cat nats-client.key nats-client.crt > combined.pem
openssl pkcs12 -export -in combined.pem -out cert.p12
keytool -importkeystore -srckeystore cert.p12 -srcstoretype pkcs12 -deststoretype pkcs12 -destkeystore nats-keystore.jks
keytool -keystore nats-keystore.jks -alias CARoot -import -file rootCa.crt -storepass password -noprompt
rm cert.p12 combined.pem
```

## Build

Build jar file from this project(cstar-audit-nats-trigger.jar) and copy it to $CASSANDRA_HOME/triggers 
directory for example:

```
sbt assembly && \
sudo cp ./target/cstar-audit-nats-trigger-assembly-1.0.jar /etc/cassandra/triggers/
```

## Deploy
```
cp ./target/scala-2.11/cstar-audit-nats-trigger-assembly-1.0.jar /etc/cassandra/triggers/
```

## Credits

Much of the work here was first done in kafka:
https://gitlab.com/rahasak-labs/siddhi/-/tree/master/siddhi-trigger