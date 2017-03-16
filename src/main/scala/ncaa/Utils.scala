package ncaa

import java.io.File

import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import org.apache.spark.sql.{Encoder, SparkSession}
import org.jsoup.Jsoup
import com.github.tototoshi.csv._

import scala.reflect.ClassTag

object Utils {

//  def parseTeams(document: Document): Iterator[Team] = {
//    // TODO: put the regex in the URL class itself
//    val links = document.select("a[href~=/team/[0-9]*/[0-9]*$]")
//    links.iterator().asScala.map { link =>
//      val url = NCAAUrl.fromString(link.attr("href")).asInstanceOf[TeamURL]
//      Team(url.teamID, url.yearCodes(url.yearID), link.text().trim())
//    }
//  }

  def writeRecords(records: Iterator[Schemas]): Unit = {}

  def storePage(page: Page): Unit = {
    page match {
      case p: AllTeamsPage =>
//        val f = new File("teams.csv")
        val writer = CSVWriter.open("teams.csv", append = true)
        writer.writeAll(p.getAllTeams.map(t => Seq(t.teamName, t.teamID, t.year)).toSeq)
        writer.close()
    }
    println(s"stored page $page")
  }

//  def parseTeamPage(): Unit = {
//    val lines = scala.io.Source.fromFile("/Users/sethhendrickson/Downloads/scrawla/SampleTeamPage")
//    val text = lines.foldLeft("") { case (acc, line) => acc + line}
//    val doc = Jsoup.parse(text)
//    val page = new TeamPage(doc, TeamURL.fromString("http://stats.ncaa.org/team/255/12480"))
//    page.getSchedule.foreach {
//      println
//    }
//  }

}
