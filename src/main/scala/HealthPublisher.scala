import akka.actor.{ActorSystem, Actor, Props, ActorLogging}
import akka.zeromq._
import akka.serialization.SerializationExtension
import java.lang.management.ManagementFactory
import java.text.SimpleDateFormat
import java.util.Date
import akka.util.duration._

case object Tick

case class Heap(timestamp: Long, used: Long, max: Long)

case class Load(timestamp: Long, loadAverage: Double)

class HealthProbe extends Actor {
  val pubSocket = context.system.newSocket(SocketType.Pub, Bind("tcp://127.0.0.1:1235"))
  val memory = ManagementFactory.getMemoryMXBean
  val os = ManagementFactory.getOperatingSystemMXBean
  val ser = SerializationExtension(context.system)

  override def preStart() {
    println("Entered HealthProbe preStart()")
    context.system.scheduler.schedule(1 second, 1 second, self, Tick)
  }

  override def postRestart(reason: Throwable) {
    println("Entered HealthProbe postRestart()")
    // don't call preStart, only schedule once
  }

  def receive: Receive = {
    case Tick ⇒
      println("HealthProbe got a Tick")
      val currentHeap = memory.getHeapMemoryUsage
      val timestamp = System.currentTimeMillis

      // use akka SerializationExtension to convert to bytes
      val heapPayload = ser.serialize(Heap(timestamp, currentHeap.getUsed, currentHeap.getMax)).fold(throw _, identity)
      // the first frame is the topic, second is the message
      println("HealthProbe about to publish health.heap")
      pubSocket ! ZMQMessage(Seq(Frame("health.heap"), Frame(heapPayload)))

      // use akka SerializationExtension to convert to bytes
      val loadPayload = ser.serialize(Load(timestamp, os.getSystemLoadAverage)).fold(throw _, identity)
      // the first frame is the topic, second is the message
      println("HealthProbe about to publish health.load")
      pubSocket ! ZMQMessage(Seq(Frame("health.load"), Frame(loadPayload)))
  }
}

/** Subscriber that logs the information.
 * It subscribes to all topics starting with "health", i.e. both Heap and Load events. */
class Logger extends Actor with ActorLogging {
  println("Logger about to subscribe to health")
  context.system.newSocket(SocketType.Sub, Listener(self), Connect("tcp://127.0.0.1:1235"), Subscribe("health"))
  val ser = SerializationExtension(context.system)
  val timestampFormat = new SimpleDateFormat("HH:mm:ss.SSS")

  def receive = {
    // the first frame is the topic, second is the message
    case m: ZMQMessage if m.firstFrameAsString == "health.heap" ⇒
      println("Logger got a ZMQmessage for health.heap")
      ser.deserialize(m.payload(1), classOf[Heap]) match {
        case Right(Heap(timestamp, used, max)) ⇒
          log.info("Used heap {} bytes, at {}", used, timestampFormat.format(new Date(timestamp)))
        case Left(e) ⇒ throw e
      }

    case m: ZMQMessage if m.firstFrameAsString == "health.load" ⇒
      println("Logger got a ZMQMessage health.load")
      ser.deserialize(m.payload(1), classOf[Load]) match {
        case Right(Load(timestamp, loadAverage)) ⇒
          log.info("Load average {}, at {}", loadAverage, timestampFormat.format(new Date(timestamp)))
        case Left(e) ⇒ throw e
      }

    case m => println("Logger got a " + m)
  }
}


/** Subscriber keeps track of used heap and warns if too much heap is used.
  * It only subscribes to Heap events. */
class HeapAlerter extends Actor with ActorLogging {
  println("HeapAlerter about to subscribe to health.heap")
  context.system.newSocket(SocketType.Sub, Listener(self), Connect("tcp://127.0.0.1:1235"), Subscribe("health.heap"))
  val ser = SerializationExtension(context.system)
  var count = 0

  def receive = {
    // the first frame is the topic, second is the message
    case m: ZMQMessage if m.firstFrameAsString == "health.heap" ⇒
      println("HeapAlerter got a ZMQMessage for health.heap")
      ser.deserialize(m.payload(1), classOf[Heap]) match {
        case Right(Heap(timestamp, used, max)) ⇒
          if ((used.toDouble / max) > 0.9) count += 1
          else count = 0
          if (count > 10) log.warning("Need more memory, using {} %", (100.0 * used / max))
        case Left(e) ⇒ throw e
      }

    case m => println("HeapAlerter got a " + m)
  }
}

object Main extends App {
  val system = ActorSystem()
  val healthProbeActor = system.actorOf(Props[HealthProbe], name = "health")
  val loggerActor      = system.actorOf(Props[Logger],      name = "logger")
  val heapAlerterActor = system.actorOf(Props[HeapAlerter], name = "alerter")
}
