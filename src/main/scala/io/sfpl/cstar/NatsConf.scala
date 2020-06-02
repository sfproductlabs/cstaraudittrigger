package io.sfpl.cstar

import com.typesafe.config.ConfigFactory
import java.io.File
import scala.util.Try

trait NatsConf {
  // config object
  val baseConfig = ConfigFactory.load("nats.conf")
  val natsConf = ConfigFactory.parseFile(new File("/etc/cassandra/nats.conf")).withFallback(baseConfig).resolve()
  // nats conf
  lazy val natsAddr = Try(natsConf.getString("nats.addr")).getOrElse("dev.localhost:9092")
  lazy val natsGroup = Try(natsConf.getString("nats.group")).getOrElse("aplosg")
  lazy val natsTopic = Try(natsConf.getString("nats.topic")).getOrElse("aplos")
  lazy val natsRegistrarApiTopic = Try(natsConf.getString("nats.registrarapi-topic")).getOrElse("registrarapi")
}

