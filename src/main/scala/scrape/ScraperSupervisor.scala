package scrape

import java.net.URL

import akka.actor.TypedActor.Supervisor
import akka.actor.{Actor, ActorRef, Props}
import akka.routing.BalancingPool
import org.jsoup.nodes.Document
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.reflect.ClassTag
import scala.concurrent.ExecutionContext.Implicits.global

case class ScraperFinished(url: URL, html: Document)
case class Scrape(url: URL, delay: Duration)
case class CreateScrapers(n: Int)
case class ScrapeURLS(urls: Seq[URL])

class ScraperSupervisor(val resultHandler: ActorRef) extends Actor with Supervisor {

  implicit val timeout = Timeout(5.seconds)
  val router = context.system.actorOf(BalancingPool(8).props(Props[ScraperActor]))
  override def receive = {
    case ScrapeURLS(urls) =>
      println("scraping urls")
      val tmp = urls.map { url =>
        val reply = (router ? Scrape(url, 1.seconds))
        reply.map(_.asInstanceOf[ScrapeResult])
      }
      val fseq = Future.sequence(tmp)
      println(s"type of fseq: ${fseq.getClass().getName()}")
      sender() ! fseq
    case ScraperFinished(url, doc) =>
      resultHandler ! ScrapeSuccess(url, doc)
      println(s"Scraper finished", sender())
    case ScraperTimeout(url) =>
  }
}
