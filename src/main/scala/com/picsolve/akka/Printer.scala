package com.picsolve.akka

import akka.actor.Actor
import akka.actor.ActorLogging
import com.picsolve.events.{AddPassEvent, AssociationEvent}


class Printer extends Actor with ActorLogging {

  def receive: Receive = {
    case ev: AssociationEvent =>
      println(s"Association received: ${ev.message}")
    case ev: AddPassEvent =>
      println(s"Add Pass received: ${ev.message}")
    case s: String =>
      println(s"String received: $s")
    case other =>
      println(s"Not sure what's this : $other")
  }
}


