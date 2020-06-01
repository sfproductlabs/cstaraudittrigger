# C* Audit - Nats.io Trigger for Elassandra & Cassandra

Cassandra audit trigger built with scala sbt project for nats.io messaging. This is an interim requirement for my clients before Cassandra 4.

## build

build jar file from this project(cstar-audit-nats-trigger.jar) and copy it to /var/lib/cassandra/lib/triggers 
directory

```
sbt assembly
```

## credits

Much of the work here was first done in kafka:
https://gitlab.com/rahasak-labs/siddhi/-/tree/master/siddhi-trigger