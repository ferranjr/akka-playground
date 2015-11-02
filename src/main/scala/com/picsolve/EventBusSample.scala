package com.picsolve

import _root_.akka.actor.{ActorRef, Props, ActorSystem}
import com.picsolve.akka.Printer
import com.picsolve.events._

object EventBusSample extends App {

  implicit val system = ActorSystem("EventBusSampleSystem")

//  val printer = Subscriber {
//    case AssociationEvent(msg) => println(s"$msg")
//    case other => "Unknown"
//  }

  val subscriber: ActorRef = system.actorOf(Props[Printer])
  EventBus.subscribe(subscriber, classOf[AssociationEvent])

  val subscriber2: ActorRef = Subscriber{
    case AssociationEvent(msg) => println(s"Association just happened: $msg")
    case other => "Unknown"
  }
  EventBus.subscribe(subscriber2, classOf[AssociationEvent])

  (1 to 10) foreach { i =>
    EventBus.publish(AssociationEvent(s"Association $i"))
  }

  (1 to 10) foreach { i =>
    EventBus.publish(AddPassEvent(s"Add Pass $i"))
  }

}
