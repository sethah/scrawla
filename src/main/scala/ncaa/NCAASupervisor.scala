package ncaa

import java.net.URL

import akka.actor.{Actor, Props}
import akka.routing.BalancingPool
import org.jsoup.nodes.Document
import scrape.{ScrapeSuccess, ScraperSupervisor}

case class ParsePage(url: NCAAUrl, doc: Document)
case class ParsedPage(page: Page)
case class StorePage(page: Page)
// TODO: probably can separate this out into your own actor which communicates with the
// scraper supervisor so the logic isn't all mangled together.
//class NCAASupervisor extends ScraperSupervisor[NCAAScraper] {
//
//  override def delegateSuccess(url: URL, doc: Document): Unit = {
//    val ncaaURL = NCAAUrl.fromString(url.toString)
//    ncaaURL match {
//      case u: AllTeamsURL => router ! ParseTeams(u, doc)
//      case TeamURL(teamID, yearID) =>
//    }
//  }
//
//  override def delegateMessage(msg: Any): Unit = {
//    msg match {
//      case ParseSuccess(records) => router ! WriteRecords(records)
//      case WriteSuccess(n) => println(s"Wrote $n records successfully")
//    }
//  }
//}

class NCAASupervisor extends Actor {
  val router = context.system.actorOf(BalancingPool(8).props(Props[NCAAParser]))
  override def receive = {
    case ScrapeSuccess(url, doc) => {
      router ! ParsePage(NCAAUrl.fromString(url.toString()), doc)
    }
    case ParsedPage(page: Page) =>
      router ! StorePage(page)
  }
}

class NCAAParser extends Actor {
  override def receive = {
    case ParsePage(url, doc) => {
      val page = url match {
        case aurl: AllTeamsURL => {
          val p = new AllTeamsPage(aurl, doc)
//          p.getAllTeams.foreach(println)
          p
        }
        case turl: TeamURL => new TeamPage(turl, doc)
      }
      sender() ! ParsedPage(page)
    }
    case StorePage(page) =>
      Utils.storePage(page)
  }
}
