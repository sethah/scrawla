package scrape

import java.net.URL

import akka.actor.Actor
import org.jsoup.nodes.Document

import scala.util.{Failure, Success, Try}

case class ScraperTimeout(url: URL)

sealed trait ScrapeResult
case class ScrapeSuccess(url: URL, html: Document) extends ScrapeResult
case class ScrapeFailure(url: URL, e: Throwable) extends ScrapeResult


class ScraperActor extends Actor {

  override def receive = {
    case Scrape(url, delay) =>
      Thread.sleep(delay.toMillis)
      println(s"Delay: ${delay.toMillis}, Scraping url: ${url.toString}")
      val response = Try(Utils.getHTML(url))
      response match {
        case Success(resp) => {
          println(s"Done scraping url: ${url.toString}")
          sender() ! ScrapeSuccess(url, resp)
        }
        case Failure(e) =>
          sender() ! ScrapeFailure(url, e)
      }
  }
}
