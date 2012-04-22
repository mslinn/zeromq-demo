package com.micronautics.zeromq.benchmark

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.serialization.SerializationExtension
import akka.zeromq.{zeromqSystem, Connect, Listener, SocketType, Subscribe, ZMQMessage}
import java.text.SimpleDateFormat
import java.util.Date
import com.micronautics.zeromq.{Heap, Load}

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
  val system = ActorSystem()
  val loggerActorRef = system.actorOf(Props[LogSubscriber], name = "logger")
}
