import akka.actor.ActorSystem
import akka.zeromq._

/**
 * @author Mike Slinn */

class Publisher {
  val system = ActorSystem()
  val publisherEndPt = Bind("tcp://127.0.0.1:2554")
  val payload = "payload"
  val pubSocket = ZeroMQExtension(system).newSocket(SocketType.Pub, publisherEndPt)
  // To publish messages to a topic, use two Frames with the topic in the first frame.
  pubSocket ! ZMQMessage(Seq(Frame("foo.bar"), Frame(payload)))
}
