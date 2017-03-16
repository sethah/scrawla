package ncaa

import java.net.URL

sealed trait NCAAUrl {

  val prefix = "http://"

  val domain = "stats.ncaa.org"

  def toJava: URL = new URL(toString)

}

object NCAAUrl {

  val TeamRegex = ".*?/team/([0-9]+)/([0-9]+)$".r
  val AllTeamsRegex = ".*?/team/inst_team_list\\?academic_year=([0-9]+).+".r
  val BoxRegex = ".*?/game/index/([0-9]+)\\?org_id=([0-9]+).+".r
  private[ncaa] val yearCodes = Map(12480 -> 2017, 12260 -> 2016, 12020 -> 2015)
  def getYearCode(year: Int): Int = {
    require(year > 2000 && year < 2020)
    yearCodes.find { case (_, v) => v == year }.map(_._1)
      .getOrElse(throw new IllegalArgumentException(s"year $year doesn't exist"))
  }
  def getYear(yearCode: Int): Int = {
    yearCodes(yearCode)
  }

  def fromString(s: String): NCAAUrl = {
    s match {
      case TeamRegex(teamID, yearID) =>
        TeamURL(teamID.toInt, yearID.toInt)
      case AllTeamsRegex(year) => {
        AllTeamsURL(year.toInt)
      }
      case BoxRegex(gameID, homeID) => BoxURL(gameID.toInt, homeID.toInt)
      case _ => throw new IllegalArgumentException(s"Invalid url: $s")
    }
  }
}

case class TeamURL(teamID: Int, yearID: Int) extends NCAAUrl {
  require(NCAAUrl.yearCodes.contains(yearID))
  def year: Int = NCAAUrl.getYear(yearID)
  override def toString: String = prefix + domain + "/team/" + teamID + "/" + yearID
}

object TeamURL {
  def apply(team: Team): TeamURL = TeamURL(team.teamID, NCAAUrl.getYearCode(team.year))
  def fromString(s: String): TeamURL = NCAAUrl.fromString(s).asInstanceOf[TeamURL]
}

case class AllTeamsURL(year: Int) extends NCAAUrl {
  override def toString: String = {
    prefix + domain + s"/team/inst_team_list?" +
      s"academic_year=$year&conf_id=-1&division=1&sport_code=MBB"
  }
}

object AllTeamsURL {
  def fromString(s: String): AllTeamsURL = NCAAUrl.fromString(s).asInstanceOf[AllTeamsURL]
}

trait GameURL extends NCAAUrl {
  def gameID: Int
  def homeID: Int
}

case class BoxURL(gameID: Int, homeID: Int) extends GameURL {
  override def toString: String = {
    prefix + domain + s"/game/index/$gameID?org_id=$homeID"
  }
}
