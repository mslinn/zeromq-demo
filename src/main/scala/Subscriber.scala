import akka.actor.{Actor, ActorSystem, Props}
import akka.zeromq._

/**
 * @author Mike Slinn */
class Subscriber {
  val system = ActorSystem("Subscriber")
  val listener = system.actorOf(Props(new Actor {
    def receive: Receive = {
      case Connecting ⇒ println("Subscriber connecting")
      case m: ZMQMessage ⇒ println("Subscriber got a ZMQMessage " + m.toString())
      case m ⇒ println("Subscriber got an unexpected message " + m)
    }
  }))
  val subscriberEndPt = Connect("tcp://127.0.0.1:1234")
  // subscribe to all messages from the publisher
  val subSocket      = system.newSocket(SocketType.Sub, Listener(listener), subscriberEndPt, SubscribeAll)
  // subscribe to a topic (prefix match)
  val subTopicSocket = system.newSocket(SocketType.Sub, Listener(listener), subscriberEndPt, Subscribe("foo.bar"))
  // unsubscribe from a topic
  subTopicSocket ! Unsubscribe("foo.bar")
}
