package com.picsolve

import _root_.akka.actor.{ActorRef, Props, ActorSystem}
import com.picsolve.akka.Printer
import com.picsolve.events._
import com.thenewmotion.akka.rabbitmq._
import play.api.libs.json.{JsError, JsSuccess, Json}

object EventBusSample extends App {

  implicit val system = ActorSystem("EventBusSampleSystem")

  //
  val connFactory = new ConnectionFactory
  connFactory.setHost("192.168.99.100")

  val connection = system.actorOf(ConnectionActor.props(connFactory), "rabbitMQ")
  val exchange = "amq.fanout"
  val assocQueue = "test.associations"


  def setupPublisher(channel: Channel, self: ActorRef) {
    val queue = channel.queueDeclare(assocQueue, false, false, false, null).getQueue
    println(
      s"""
         | Set up publisher
         |  > $queue
         |
       """.stripMargin)
    channel.queueBind(queue, exchange, "")
  }
  connection ! CreateChannel(ChannelActor.props(setupPublisher), Some("publisher"))

  def setupSubscriber(channel: Channel, self: ActorRef) {
    val queue = channel.queueDeclare(assocQueue, false, false, false, null).getQueue
    println(
      s"""
         | Set up publisher
         |  > $queue
         |
       """.stripMargin)
    channel.queueBind(queue, exchange, "")
    val consumer = new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: Array[Byte]) {
        Json.parse(body).validate[AssociationEvent].asOpt.foreach( EventBus.publish )
      }
    }
    channel.basicConsume(queue, true, consumer)
  }
  connection ! CreateChannel(ChannelActor.props(setupSubscriber), Some("subscriber"))

  def fromBytes(x: Array[Byte]) = new String(x, "UTF-8")

  val publisher = system.actorSelection("/user/rabbitMQ/publisher")

  def publish(msg: String)(channel: Channel) {
    channel.basicPublish(
      exchange, "", null,
      Json.toJson(AssociationEvent(msg)).toString().getBytes("UTF-8"))
  }

  val subscriber: ActorRef = system.actorOf(Props[Printer])
  EventBus.subscribe(subscriber, classOf[AssociationEvent])

  (1 to 100) foreach { i =>
    publisher ! ChannelMessage(publish(s"Association $i"), dropIfNoChannel = false)
  }

}
