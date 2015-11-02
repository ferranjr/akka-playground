package com.picsolve.events

import akka.actor.Actor.Receive
import akka.actor.{Props, Actor, ActorSystem, ActorRef}
import akka.event.{ActorEventBus, SubchannelClassification}
import akka.util.Subclassification
import com.picsolve.events
import play.api.libs.json.{JsValue, Json, Writes}


trait Event

trait EventSerializer[A <: Event]

case object Event extends Event {
  def serializer[A <: Event](name: String)(block: (A => JsValue)) = new Writes[A] {
    def writes(o: A): JsValue = Json.obj(
      "event" -> name,
      "data" -> block(o)
    )
  }
}

case class AssociationEvent(message: String) extends Event
case class AddPassEvent(message: String) extends Event


class EventBus extends ActorEventBus with SubchannelClassification {
  type Classifier = Class[_ <: Event]
  type Event = com.picsolve.events.Event

  override protected implicit def subclassification: Subclassification[Classifier] = new Subclassification[Classifier] {
    override def isEqual(x: Class[_ <: events.Event], y: Class[_ <: events.Event]): Boolean = x == y

    override def isSubclass(x: Class[_ <: events.Event], y: Class[_ <: events.Event]): Boolean = y.isAssignableFrom(x)
  }

  protected def publish(event: Event, subscriber: Subscriber): Unit = subscriber ! event

  protected def classify(event: Event): Classifier = event.getClass
}

object EventBus {

  val localBus = new EventBus

  def subscribe(actor: ActorRef, to: Class[_ <: Event]) = {
    localBus.subscribe( actor, to )
  }

  def publish(ev: Event) = {
    localBus.publish(ev)
  }

}

/**
 * A subscriber of events.
 */
object Subscriber {
  /**
   * Create a subscriber by passing a receive block.
   * @param receiveBlock
   * @param system
   * @return
   */
  def apply(receiveBlock: Receive)(implicit system: ActorSystem) = {
    val subscriber = system.actorOf(Props(new Actor {
      def receive: Receive = receiveBlock
    }))

    EventBus.subscribe(subscriber, classOf[Event])
    subscriber
  }
}
