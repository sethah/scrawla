import java.net.URL

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.routing.BalancingPool
import com.github.tototoshi.csv.CSVReader
import ncaa._
import org.jsoup.nodes.Element
import scrape._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{Await, Future}
import scala.io.Source

//import org.apache.commons.validator.routines.UrlValidator
import org.jsoup.Jsoup
import scala.concurrent.duration._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global

object ScrapeExample extends App {

  implicit val timeout = Timeout(5.seconds)
  val system = ActorSystem("ScheduleScraper")
  val ncaaRef = system.actorOf(Props[NCAASupervisor])
  val scraper = system.actorOf(Props(new ScraperSupervisor(ncaaRef)))
//  val scraper = new ScraperSupervisor(system.actorOf(Props[NCAASupervisor]))
  val urls = Seq(2015, 2016).map(AllTeamsURL.apply)
  val reader = CSVReader.open("teams.csv")
  val teamURLS = reader.iterator.map { line =>
    val Seq(tname, tid, year) = line
    val tm = Team(tid.toInt, year.toInt, tname)
    TeamURL(tm)
  }.toList
  val results = scraper ? ScrapeURLS(teamURLS.map(_.toJava))
  try {
    println(results.isInstanceOf[Future[Future[List[_]]]])
    val summary = results.asInstanceOf[Future[Future[List[_]]]].flatMap { l =>
      l.map { lst =>
        (lst.count(_.isInstanceOf[ScrapeSuccess]),
          lst.count(_.isInstanceOf[ScrapeFailure]))
      }
    }
    val s = Await.result(summary, 300.seconds)
    println("s returned")
    println(s)
    scraper ! PoisonPill
    system.terminate()
  } finally {
    scraper ! PoisonPill
    system.terminate()
  }



//  scraper ! ScrapeURLS(urls.map(_.toJava))
//  import system.dispatcher
//  val url = new URL("http://stats.ncaa.org/team/inst_team_list?academic_year=2015&conf_id=-1&division=1&sport_code=MBB")
//  val supervisor = system.actorOf(Props[NCAASupervisor], "supervisor")
//
//  Thread.sleep(8000)
//  println("about to terminate")
//  val supervisor
//  val url = new URL("http://stats.ncaa.org/team/110.0/12480")
//  val lines = scala.io.Source.fromFile("/Users/sethhendrickson/Downloads/scrawla/TeamsPage")
//  val text = lines.foldLeft("") { case (acc, line) => acc + line}
//  val doc = Jsoup.parse(text)
//  val links = doc.select("a[href~=/team/[0-9]*/[0-9]*$]")
//  val it = links.iterator().asScala
//  it.foreach { println}

//  val link: String = url.toString
//  val response = Jsoup.connect(link).ignoreContentType(true)
//    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
//    .timeout(10 * 1000)
//    .get()
//  val links = response.select("a[href]")
//  println(links
////  println(response.body())
//  val doc = response.parse()
//  val scheduleTable = doc.select("table.mytable").first()
//  val rows = scheduleTable.select("tr")
//  println(rows.iterator().next())
}
