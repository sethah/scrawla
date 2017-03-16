package examples

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern._
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

object AskPattern extends App {
  case object AskName
  case class NameResponse(name: String)
  class NamedActor(name: String) extends Actor {
    def receive = {
      case AskName =>
        sender ! NameResponse(name)
    }
  }

  implicit val timeout: Timeout = Timeout(1.seconds)
  val system = ActorSystem("SimpleSystem")
  val actor1 = system.actorOf(Props(new NamedActor("Seth")), "NamedActor")
  val answer = actor1 ? AskName
  answer.foreach(println)
  // terminate could happen before the actor responds!
  system.terminate()
}
