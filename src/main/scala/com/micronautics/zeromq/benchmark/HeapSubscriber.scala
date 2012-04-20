package com.micronautics.zeromq.benchmark

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.serialization.SerializationExtension
import akka.zeromq.zeromqSystem
import akka.zeromq.Connect
import akka.zeromq.Listener
import akka.zeromq.SocketType
import akka.zeromq.Subscribe
import akka.zeromq.ZMQMessage
import com.micronautics.zeromq.Heap

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
  val system = ActorSystem()
  val heapAlerterActorRef = system.actorOf(Props[HeapSubscriber], name = "alerter")
}
