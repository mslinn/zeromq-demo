package com.micronautics.zeromq.benchmark

import akka.serialization.SerializationExtension
import akka.util.duration.intToDurationInt
import akka.zeromq.zeromqSystem
import akka.zeromq.Bind
import akka.zeromq.Frame
import akka.zeromq.SocketType
import akka.zeromq.ZMQMessage
import java.lang.management.ManagementFactory
import com.micronautics.zeromq.{Load, Heap, Tick}
import com.micronautics.util.ByteFormatter
import akka.actor._

class HealthPublisher extends Actor with ActorLogging {
  val pubSocket = context.system.newSocket(SocketType.Pub, Bind("tcp://127.0.0.1:1235"))
  val memory = ManagementFactory.getMemoryMXBean
  val os = ManagementFactory.getOperatingSystemMXBean
  val ser = SerializationExtension(context.system)
  var tick: Long = 0L
  val tenSeconds: Long = 10L
  var startTime = System.nanoTime

  override def preStart() {
    log.debug("Entered HealthPublisher preStart()")
    //context.system.scheduler.schedule(0 second, 1 second, self, Tick)
    self ! Tick
  }

  override def postRestart(reason: Throwable) {
    log.debug("Entered HealthPublisher postRestart()")
    // don't call preStart, only schedule once
  }

  def receive: Receive = {
    case Tick ⇒
      log.debug("HealthPublisher got a Tick")
      tick += 1
      val currentHeap = memory.getHeapMemoryUsage
      val timestamp = System.currentTimeMillis

      // use akka SerializationExtension to convert to bytes
      val heapPayload = ser.serialize(Heap(timestamp, currentHeap.getUsed, currentHeap.getMax)).fold(throw _, identity)
      // the first frame is the topic, second is the message
      log.debug("HealthPublisher about to publish health.heap")
      pubSocket ! ZMQMessage(Seq(Frame("health.heap"), Frame(heapPayload)))

      // use akka SerializationExtension to convert to bytes
      val loadPayload = ser.serialize(Load(timestamp, os.getSystemLoadAverage)).fold(throw _, identity)
      // the first frame is the topic, second is the message
      log.debug("HealthPublisher about to publish health.load")
      pubSocket ! ZMQMessage(Seq(Frame("health.load"), Frame(loadPayload)))
      if (tick % 10000 == 0) {
        val mps = throughput
        val bytes = heapPayload.length
        val bytesPerSecond = mps * bytes
        log.error("Throughput: %d messages/sec; %s/message; total %s/sec".format(mps, ByteFormatter.format(bytes), ByteFormatter.format(bytesPerSecond)))
      }
      self ! Tick

    case m =>
      log.debug(m.toString)
  }

  def throughput = {
    val elapsedSeconds = (System.nanoTime - startTime) / 1000000000.0
    //log.error("elapsedSeconds=%f; tick=%d".format(elapsedSeconds, tick))
    val speed = (tick.toDouble / elapsedSeconds).toLong
    tick = 0
    startTime = System.nanoTime
    speed
  }
}

object HealthPublisher extends App {
  val system = ActorSystem()
  val healthPublisherActorRef = system.actorOf(Props[HealthPublisher], name = "health")
}
