package ncaa

import org.joda.time.DateTime

trait Table {
  def name: String
}

case object TeamTable extends Table {
  override val name: String = "teams"
}

case object GameTable extends Table {
  override val name: String = "games"
}

sealed trait Schemas extends Product {
  def table: Table
}

case class Team(teamID: Int, year: Int, teamName: String) extends Schemas {

  override val table: Table = TeamTable

}

trait Game extends Schemas {
  def date: DateTime
  override def table: Table = GameTable
}


case class CompletedGame(
    gameID: Int,
    date: DateTime,
    opponent: Team,
    outcome: Boolean,
    score: Int,
    opponentScore: Int,
    location: Int,
    overtime: Int) extends Game

case class UnplayedGame(date: DateTime, opponent: String, location: Int) extends Game


