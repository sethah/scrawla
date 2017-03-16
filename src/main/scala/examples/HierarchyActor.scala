package examples

import akka.actor.SupervisorStrategy.{Restart, Resume}
import akka.actor.TypedActor.Supervisor
import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps

object HierarchyActor extends App {
  case object CreateChild
  case class SignalChildren(order: Int)
  case class PrintSignal(order: Int)
  case class DivideNumbers(num: Int, den: Int)
  case object BadStuff
  class ParentActor extends Actor with Supervisor {
    private var id = 0
    private val children = collection.mutable.Buffer[ActorRef]()
    def receive = {
      case CreateChild =>
        context.actorOf(Props[ChildActor], "child" + id)
        id += 1
      case SignalChildren(n) =>
        context.children.foreach { child => child ! PrintSignal(n)}
    }

    override val supervisorStrategy = OneForOneStrategy(loggingEnabled = false) {
      case ae: ArithmeticException => Resume
      case e: Exception => Restart
    }
  }

  class ChildActor extends Actor {
    println("Child created.")
    def receive = {
      case PrintSignal(n) => println(self + "|" + n)
      case DivideNumbers(n, d) => println(n / d)
      case BadStuff => throw new RuntimeException("bad stuff")
    }
    override def preStart(): Unit = {
      super.preStart()
      println("preStart")
    }
    override def postStop(): Unit = {
      super.postStop()
      println("postStop")
    }

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      super.preRestart(reason, message)
      println("preRestart")
    }

    override def postRestart(reason: Throwable): Unit = {
      super.postRestart(reason)
      println("postRestart")
    }
  }

  implicit val timeout: Timeout = Timeout(1.seconds)
  val system = ActorSystem("Hierarchy")
  val parent1 = system.actorOf(Props(new ParentActor), "Parent1")
  val parent2 = system.actorOf(Props(new ParentActor), "Parent2")
  parent1 ! CreateChild

  val child0 = system.actorSelection("/user/Parent1/child0")
  child0 ! DivideNumbers(4, 0)
  child0 ! DivideNumbers(4, 2)
  child0 ! BadStuff

  Thread.sleep(1000)
  system.terminate()
}
