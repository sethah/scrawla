package ncaa

import org.joda.time.format._
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConverters._
import scala.util.Try

sealed trait Page {
  def url: NCAAUrl
  override def toString: String = url.toString
}

case class AllTeamsPage(val url: AllTeamsURL, private val doc: Document) extends Page {

  def getAllTeams: Iterator[Team] = {
    val links = doc.select("a[href~=/team/[0-9]*/[0-9]*$]")
    links.iterator().asScala.map { link =>
      val url = TeamURL.fromString(link.attr("href"))
      Team(url.teamID, url.year, link.text().trim())
    }
  }
}

class TeamPage(val url: TeamURL, private val doc: Document) extends Page {

  private val dateFormat: DateTimeFormatter = DateTimeFormat.forPattern("MM/dd/yyyy")

  def getScheduleRows(): Iterator[Element] = {
    val rows = doc.select("tr")
    rows.iterator().asScala.filter { row =>
      val cells = row.select("td").iterator().asScala.toSeq
      cells.exists { c =>
        Try(dateFormat.parseDateTime(c.text().trim())).isSuccess
      } && cells.length == 3
    }
  }

  def getSchedule: Iterator[Game] = {
    val scheduleRows = getScheduleRows()
    scheduleRows.map(parseScheduleRow)
  }

  def parseScheduleRow(row: Element): Game = {
    val cells = row.select("td").iterator().asScala
    val date = dateFormat.parseDateTime(cells.next().text())
    val opponentCell = cells.next()
    val outcomeCell = cells.next()
    val (opponentString, location) = parseOpponent(opponentCell.text().trim())
    if (outcomeCell.select("a").isEmpty()) {
      // game has not been played
      UnplayedGame(date, opponentString, location)
    } else {
      val opponentURL = Option(opponentCell.select("a")).map { lnks =>
        NCAAUrl.fromString(lnks.first().attr("href")).asInstanceOf[TeamURL]
      }.getOrElse(throw new IllegalStateException(s"no opponent link was found: $opponentCell"))
      val opponent = Team(opponentURL.teamID, opponentURL.year, opponentString)
      val outcomeLink = outcomeCell.select("a").first().attr("href")
      val outcomeURL = NCAAUrl.fromString(outcomeLink).asInstanceOf[BoxURL]
      val (outcome, score, opponentScore, ot) = parseOutcome(outcomeCell.text())
      CompletedGame(outcomeURL.gameID, date, opponent, outcome, score, opponentScore, location,
        ot)
    }
  }

  def parseOpponent(opponentString: String): (String, Int) = {
    if (opponentString.trim().startsWith("@")) {
      // away game
      (opponentString.replace("@", "").trim(), -1)
    } else if (opponentString.contains("@")) {
      // neutral site
      val Array(opp, venue) = opponentString.split("@")
      (opp.trim(), 0)
    } else {
      // home game
      (opponentString.trim(), 1)
    }
  }

  def parseOutcome(outcomeString: String): (Boolean, Int, Int, Int) = {
    // TODO: improve regex for ot capturing
    val regex = "([W|L]) ([0-9]+) - ([0-9]+)(\\s+\\([0-9]+OT\\))?".r
    outcomeString match {
      case regex(outcome, score, oppScore, ot) =>
        val numOT = Option(ot)
          .map(_.replace("OT", "").replace("(", "").replace(")", "").trim().toInt)
        (outcome == "W", score.toInt, oppScore.toInt, numOT.getOrElse(0))
      case _ => throw new IllegalStateException(s"Bad outcome string $outcomeString")
    }
  }
}
