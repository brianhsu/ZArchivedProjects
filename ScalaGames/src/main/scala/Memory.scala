package scalagames.memory

import org.newdawn.slick._


case class Point(x: Int, y: Int)

case class Card(cardNo: Int, position: Point, var isExposed: Boolean = false, var isClicked: Boolean = false) {
  
  val Height = 200
  val Width = 150

  lazy val backImage = Memory.loadImage("/Memory/cardback.jpg")
  lazy val image = Memory.loadImage("/Memory/card" + (cardNo + 1) + ".jpg")

  def isClickOnCard(mousePosition: Point) = {
    mousePosition.x > this.position.x &&
    mousePosition.x < this.position.x + Width &&
    mousePosition.y > this.position.y &&
    mousePosition.y < this.position.y + Height
  }

  def drawImage(image: Image, graphics: Graphics) {
    graphics.drawImage(
      image, position.x, position.y, position.x + Width, position.y + Height, 0, 0, 
      image.getWidth, image.getHeight
    )
  }

  def drawCard(graphics: Graphics) {
    val shouldShow = this.isExposed || this.isClicked
    shouldShow match {
      case true => drawImage(image, graphics)
      case false => drawImage(backImage, graphics)
    }
  }
}

class Game {
  
  import scala.util.Random
  private var moves: Int = 0
  private var lastCard: Option[Card] = None

  private val position = Vector(
    Point(10, 5),   Point(170, 5),   Point(330, 5),   Point(490, 5),
    Point(10, 210), Point(170, 210), Point(330, 210), Point(490, 210),
    Point(10, 415), Point(170, 415), Point(330, 415), Point(490, 415),
    Point(10, 620), Point(170, 620), Point(330, 620), Point(490, 620)
  )

  var cards = {
    val randomCardNo = Random.shuffle(Range(0, 8) ++ Range(0, 8)).toVector
    randomCardNo.zipWithIndex map { case(cardNo, index) => Card(cardNo, position(index)) }
  }

  def isAllFliped = cards.forall(_.isExposed)
  def getMoves = moves

  def drawBoard(graphics: Graphics) = cards.foreach(_.drawCard(graphics))
  def getCard(position: Point) = cards.filter(_.isClickOnCard(position)).headOption
  def clearClicked = cards.foreach(card => card.isClicked = false)
  def flipCard(position: Point): Option[Card] = {

    val clickOnCard = getCard(position).filterNot(c => c.isClicked || c.isExposed)

    clickOnCard map { card =>

      lastCard match {
        case None =>
          this.clearClicked
          card.isClicked = true
          lastCard = Some(card)
        case Some(last) =>

          card.isClicked = true

          if (last.cardNo == card.cardNo) {
            last.isExposed = true
            card.isExposed = true
          }

          lastCard = None
          moves += 1
      }

      card
    }

  }

}

object Memory {

  def loadImage(filename: String): Image = {
    new Image(this.getClass().getResourceAsStream(filename), filename, false)
  }

  def main(args: Array[String]) {
    val app = new AppGameContainer(new Memory)
    app.setDisplayMode(1024, 900, false)
    app.start()
  }

}

class Memory extends BasicGame("Memory Game")
{

  var game: Game = new Game
  var moveMessage = "Click a card to start!"
  var doneMessage = ""
  val restartMessage = "Press R to restart"

  override def init(gc: GameContainer) {}

  override def keyReleased(key: Int, char: Char) {
    if (char == 'R' || char == 'r') {
      game = new Game
      moveMessage = "Click a card to start!"
      doneMessage = ""
    }
  }

  override def mouseClicked(button: Int, x: Int, y: Int, clickCount: Int) {
    
    if (button == 0 && clickCount == 1) {
      val card = game.flipCard(Point(x, y))
      val done = game.isAllFliped

      if (done) {
        moveMessage = "Done in " + game.getMoves + " steps"
        doneMessage = "Press R to start a new game."
      } else {
        moveMessage = "Moves = " + game.getMoves
        if (card.isDefined) {
          doneMessage = "You clicked on Card " + card.get.cardNo
        }
      }
    }

  }

  override def update(gc: GameContainer, delta: Int) {}

  override def render(gc: GameContainer, g: Graphics) {
    g.drawString(moveMessage, 700, 100)
    g.drawString(doneMessage, 700, 120)
    g.drawString(restartMessage, 700, 400)
    game.drawBoard(g)
  }

}
