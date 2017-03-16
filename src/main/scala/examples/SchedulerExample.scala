package examples

import akka.actor.{Actor, ActorSystem, Props}

import scala.concurrent.duration._

object SchedulerExample extends App {

  case object Count
  class ScheduleActor extends Actor {
    var n = 0
    def receive = {
      case Count =>
        n += 1
        println(n)
    }
  }
  val system = ActorSystem("Scheduler")
  val actor = system.actorOf(Props(new ScheduleActor), "Scheduler")
  implicit val ec = system.dispatcher

  actor ! Count
  system.scheduler.scheduleOnce(1.seconds)(actor ! Count)
  val cancellable = system.scheduler.schedule(0.seconds, 100.millis, actor, Count)

  Thread.sleep(2000)
  cancellable.cancel()
  system.terminate()

}
