package ncaa

import java.net.URL

import org.jsoup.nodes.Document
import scrape.ScraperActor

case class ParseTeams(url: AllTeamsURL, body: Document)
case class ParseSuccess(records: Iterator[Schemas])
case class WriteRecords(records: Iterator[Schemas])
case class WriteSuccess(n: Int)
//class NCAAScraper extends ScraperActor {
//  override def delegateMessage(msg: Any): Unit = {
//    msg match {
//      case ParseTeams(url, body) =>
//        println("parsing teams!")
//        val teams = Utils.parseTeams(body)
//        sender() ! ParseSuccess(teams)
//      case WriteRecords(records) =>
//        Utils.writeRecords(records)
//        sender() ! WriteSuccess(records.length)
//    }
//  }
//}
