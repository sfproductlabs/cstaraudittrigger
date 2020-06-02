package io.sfpl.cstar

import com.typesafe.config.ConfigFactory

import scala.util.Try

trait NatsConf {
  // config object
  val natsConf = ConfigFactory.load("nats.conf")

  // nats conf
  lazy val natsAddr = Try(natsConf.getString("nats.addr")).getOrElse("dev.localhost:9092")
  lazy val natsGroup = Try(natsConf.getString("nats.group")).getOrElse("aplosg")
  lazy val natsTopic = Try(natsConf.getString("nats.topic")).getOrElse("aplos")
  lazy val natsRegistrarApiTopic = Try(natsConf.getString("nats.registrarapi-topic")).getOrElse("registrarapi")
}

