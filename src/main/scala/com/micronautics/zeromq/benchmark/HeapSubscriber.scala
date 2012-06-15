package com.micronautics.zeromq.benchmark

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.serialization.SerializationExtension
import akka.zeromq.zeromqSystem
import akka.zeromq.{Connect, Listener, SocketType, Subscribe, ZMQMessage}
import com.micronautics.zeromq.Heap
import com.typesafe.config.ConfigFactory

/** Subscriber keeps track of used heap and warns if too much heap is used.
  * It only subscribes to Heap events. */
class HeapSubscriber extends Actor with ActorLogging {
  log.debug("HeapSubscriber about to subscribe to health.heap")
  context.system.newSocket(SocketType.Sub, Listener(self), Connect("tcp://127.0.0.1:1235"), Subscribe("health.heap"))
  val ser = SerializationExtension(context.system)
  var count = 0

  def receive = {
    // the first frame is the topic, second is the message
    case m: ZMQMessage if m.firstFrameAsString == "health.heap" ⇒
      log.debug("HeapSubscriber got a ZMQMessage for health.heap")
      ser.deserialize(m.payload(1), classOf[Heap]) match {
        case Right(Heap(timestamp, used, max)) ⇒
          if ((used.toDouble / max) > 0.9) count += 1
          else count = 0
          if (count > 10) log.warning("Need more memory, using {} %", (100.0 * used / max))
        case Left(e) ⇒ throw e
      }

    case m => log.debug("HeapSubscriber got a " + m)
  }
}

object HeapSubscriber extends App {
  //system = ActorSystem() // uncomment to run without remote transport, and comment out remote transport section

  // start of definition for remote transport
  val strConf = """
                   | akka.remote.netty.hostname = "127.0.0.1"
                   | akka.remote.netty.port = 2002
                   | """.stripMargin

  val myConfig = ConfigFactory.parseString(strConf)
  val regularConfig = ConfigFactory.load()
  val combined = myConfig.withFallback(regularConfig)
  val complete = ConfigFactory.load(combined)
  val system = ActorSystem("default", complete)

  println("Running at " + system.settings.config.getString("akka.remote.netty.hostname") + ":" +
          system.settings.config.getString("akka.remote.netty.port"))
  // end of definition for remote transport

  val heapAlerterActorRef = system.actorOf(Props[HeapSubscriber], name = "alerter")
}
