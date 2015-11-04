package com.picsolve.akka

import akka.actor.Actor
import com.picsolve.events.AssociationEvent


class Printer extends Actor {

  def receive: Receive = {
    case ev: AssociationEvent =>
      println(s"Association received: ${ev.message}")
  }
}


