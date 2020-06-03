package io.sfpl.cstar

import com.typesafe.config.ConfigFactory
import java.io.File
import scala.util.Try

trait NatsConf {
  // config object
  val baseConfig = ConfigFactory.load("nats.conf")
  val natsConf = ConfigFactory.parseFile(new File("/etc/cassandra/nats.conf")).withFallback(baseConfig).resolve() 
  // nats conf
  lazy val natsServers = Try(natsConf.getString("nats.servers")).getOrElse("nats://localhost:4222")
  lazy val natsDefaultTopic = Try(natsConf.getString("nats.defaultTopic")).getOrElse("tic.log.audit")
  lazy val natsKeystorePath = Try(natsConf.getString("nats.keystorePath")).getOrElse("/etc/cassandra/nats-keystore.jks")
  lazy val natsKeystorePassword = Try(natsConf.getString("nats.keystorePassword")).getOrElse("password")
  lazy val natsTruststorePath = Try(natsConf.getString("nats.truststorePath")).getOrElse("/etc/cassandra/nats-truststore.jks")
  lazy val natsTruststorePassword = Try(natsConf.getString("nats.truststorePassword")).getOrElse("password")
}

