package com.micronautics.zeromq.benchmark

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.serialization.SerializationExtension
import akka.zeromq.{zeromqSystem, Connect, Listener, SocketType, Subscribe, ZMQMessage}
import java.text.SimpleDateFormat
import java.util.Date
import com.micronautics.zeromq.{Heap, Load}
import com.typesafe.config.ConfigFactory

/**Subscriber that logs the information.
 * It subscribes to all topics starting with "health", i.e. both Heap and Load events. */
class LogSubscriber extends Actor with ActorLogging {
  log.debug("LogSubscriber about to subscribe to health")
  context.system.newSocket(SocketType.Sub, Listener(self), Connect("tcp://127.0.0.1:1235"), Subscribe("health"))
  val ser = SerializationExtension(context.system)
  val timestampFormat = new SimpleDateFormat("HH:mm:ss.SSS")

  def receive = {
    // the first frame is the topic, second is the message
    case m: ZMQMessage if m.firstFrameAsString == "health.heap" ⇒
      log.debug("LogSubscriber got a ZMQmessage for health.heap")
      ser.deserialize(m.payload(1), classOf[Heap]) match {
        case Right(Heap(timestamp, used, max)) ⇒
          log.info("Used heap {} bytes, at {}", used, timestampFormat.format(new Date(timestamp)))
        case Left(e) ⇒ throw e
      }

    case m: ZMQMessage if m.firstFrameAsString == "health.load" ⇒
      log.debug("LogSubscriber got a ZMQMessage health.load")
      ser.deserialize(m.payload(1), classOf[Load]) match {
        case Right(Load(timestamp, loadAverage)) ⇒
          log.info("Load average {}, at {}", loadAverage, timestampFormat.format(new Date(timestamp)))
        case Left(e) ⇒ throw e
      }

    case m => println("Logger got a " + m)
  }
}

object LogSubscriber extends App {
  //system = ActorSystem() // uncomment to run without remote transport, and comment out remote transport section

  // start of definition for remote transport
  val strConf = """
                   | akka.remote.netty.hostname = "127.0.0.1"
                   | akka.remote.netty.port = 2001
                   | """.stripMargin

  val myConfig = ConfigFactory.parseString(strConf)
  val regularConfig = ConfigFactory.load()
  val combined = myConfig.withFallback(regularConfig)
  val complete = ConfigFactory.load(combined)
  val system = ActorSystem("default", complete)

  println("Running at " + system.settings.config.getString("akka.remote.netty.hostname") + ":" +
          system.settings.config.getString("akka.remote.netty.port"))
  // end of definition for remote transport

  val loggerActorRef = system.actorOf(Props[LogSubscriber], name = "logger")
}
