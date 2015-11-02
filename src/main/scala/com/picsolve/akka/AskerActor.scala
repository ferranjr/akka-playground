//package com.picsolve.akka
//
//import akka.actor.{ActorRef, Props, Actor}
//import akka.actor.Actor.Receive
//import akka.routing.RoundRobinPool
//import akka.util.Timeout
//import com.picsolve.events.{Event, EventBus}
//
//import scala.concurrent.Future
//
//
///**
// * Trait with basics needed to process a "Ask Pattern" using EventBus
// */
//sealed trait Askable extends Event {
//  def from:Option[ActorRef]
//  def withFrom(in:ActorRef):Askable
//  def withoutFrom:Askable
//}
//
///**Actor to allow us to use Ask Pattern using the EventBus
//  */
//class AskerActor extends Actor {
//
//  def receive: Receive = {
//    // Publishes into EventBus but adding the ActorRef to the sender
//    case m:Askable =>
//      EventBus.publish( m.withFrom( sender() ) )
//
//    case m =>
//      sender() ! Left(s"Wrong Askable : $m")
//  }
//
//}
//object AskerActor {
//
//  implicit private val timeout = Timeout(20 seconds)
//  implicit private val system = Akka.system
//
//  val actors = system.actorOf(Props[AskerActor].withRouter(RoundRobinPool(nrOfInstances = 5)), name = "conversion-actor")
//
//  def resultOf[T](m:Askable): Future[Either[String,T]] = ask(actors, m) collect {
//    case r:T  => Right(r)
//    case _    => Left( "Unexpected response" )
//  }
//
//}
//
