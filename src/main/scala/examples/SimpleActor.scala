package examples

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.language.postfixOps

object SimpleActor extends App {
  case class StartCounting(n: Int, other: ActorRef)
  case class Countdown(n: Int)
  class SimpleActor extends Actor {
    def receive = {
      case StartCounting(n, other) =>
        println(n)
        other ! Countdown(n - 1)
      case Countdown(n) =>
        println(self)
        if (n > 0) {
          println(n)
          sender ! Countdown(n - 1)
        } else {
          context.system.terminate()
        }
    }
  }

  val system = ActorSystem("SimpleSystem")
  val actor1 = system.actorOf(Props[SimpleActor], "SimpleActor1")
  val actor2 = system.actorOf(Props[SimpleActor], "SimpleActor2")
  actor1 ! StartCounting(10, actor2)
  system.terminate()
}
