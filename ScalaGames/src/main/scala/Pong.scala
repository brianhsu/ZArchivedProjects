package scalagames.pong

import org.newdawn.slick._
import org.lwjgl.input.Keyboard

object Pong {

  def main(args: Array[String]) {
    val app = new AppGameContainer(new Pong(600, 400))
    app.setDisplayMode(600, 400, false)
    app.start()
  }

}


class Pong(width: Int, height: Int) extends BasicGame("Hello World")
{

  var leftScore = 0
  var rightScore = 0

  object Paddle {
    val Height = 80
    val Width = 10
  
    def defaultLeft = Paddle(0, (height / 2) - Paddle.Height / 2)
    def defaultRight = Paddle(width - Paddle.Width, (height / 2) - Paddle.Height / 2)
  }
  
  case class Paddle(x: Int, y: Int) {
  
    def move(velocityY: Int): Paddle = {
      val isAtBottom = (y + velocityY + Paddle.Height > height)
      val isAtTop = (y + velocityY) < 0
      if (isAtBottom || isAtTop) this else Paddle(x, y + velocityY)
    }
  
    def draw(g: Graphics) {
      g.fillRect(x, y, Paddle.Width, Paddle.Height)
    }
  }

  object Ball {
    import scala.util.Random

    val BallRadius = 20

    def defaultBall(towardRight: Boolean) = {

      val horizontalSpeed = (Random.nextInt(240 - 120) + 120) / 60.0f
      val verticalSpeed = (Random.nextInt(180 - 60) + 60) / 60.0f
      val velocity = if (towardRight) (horizontalSpeed, verticalSpeed) else (-horizontalSpeed, -verticalSpeed)

      new Ball(width.toFloat / 2.0f, height.toFloat / 2.0f, velocity)
    }

  }

  case class Ball(x: Float, y: Float, velocity: (Float, Float)) {
    
    import Ball.BallRadius

    case object TopWall
    case object BottomWall
    case object LeftWall
    case object RightWall
    case object LeftPaddle
    case object RightPaddle
    case object NoCollision

    def getCollision = {
      val (velocityX, velocityY) = velocity
      val hitTopWall = y + velocityY <= BallRadius
      val hitBottomWall = y + velocityY + BallRadius >= height
      val hitLeftWall = (x + velocityX - BallRadius) <= Paddle.Width
      val hitRightWall = (x + velocityX + BallRadius) >= width - Paddle.Width

      val hitLeftPaddle = hitLeftWall && (y + BallRadius >= paddleLeft.y) && 
                                         (y - BallRadius < paddleLeft.y + Paddle.Height)

      val hitRightPaddle = hitRightWall && (y + BallRadius >= paddleRight.y) && 
                                           (y - BallRadius < paddleRight.y + Paddle.Height)

      if (hitTopWall) TopWall
      else if (hitBottomWall) BottomWall
      else if (hitLeftPaddle) LeftPaddle
      else if (hitRightPaddle) RightPaddle
      else if (hitLeftWall) LeftWall
      else if (hitRightWall) RightWall
      else NoCollision
    }

    def move = {

      val (velocityX, velocityY) = velocity
      val (nextSpeedX, nextSpeedY) = (velocityX + velocityX * 0.1f, velocityY + velocityY * 0.1f)
     
      getCollision match {
        case TopWall | BottomWall => new Ball(x + velocityX, y + velocityY, (nextSpeedX, -nextSpeedY))
        case LeftPaddle | RightPaddle => Ball(x + velocityX, y + velocityY, (-nextSpeedX, nextSpeedY))
        case LeftWall => rightScore += 1; Ball.defaultBall(true)
        case RightWall => leftScore += 1; Ball.defaultBall(false)
        case _ => new Ball(x + velocityX, y + velocityY, velocity)
      }
      
    }

    def draw(g: Graphics) {
      g.fillOval(x - BallRadius / 2.0f, y - BallRadius / 2.0f, BallRadius, BallRadius)
    }
  }

  var paddleLeft = Paddle.defaultLeft
  var paddleRight = Paddle.defaultRight
  var paddleLeftVelocity = 0
  var paddleRightVelocity = 0
  var ball = Ball.defaultBall(true)

  override def keyPressed(keyCode: Int, char: Char) {
    
    keyCode match {
      case Keyboard.KEY_UP   => paddleRightVelocity = -10
      case Keyboard.KEY_DOWN => paddleRightVelocity = 10
      case Keyboard.KEY_W    => paddleLeftVelocity = -10
      case Keyboard.KEY_S    => paddleLeftVelocity = 10
      case _ =>
    }

  }

  override def keyReleased(keyCode: Int, char: Char) {
    
    keyCode match {
      case Keyboard.KEY_UP   => paddleRightVelocity = 0
      case Keyboard.KEY_DOWN => paddleRightVelocity = 0
      case Keyboard.KEY_W    => paddleLeftVelocity = 0
      case Keyboard.KEY_S    => paddleLeftVelocity = 0
      case Keyboard.KEY_R    => resetGame()
      case _ =>
    }

  }

  def resetGame() {
    leftScore = 0
    rightScore = 0
    paddleLeft = Paddle.defaultLeft
    paddleRight = Paddle.defaultRight
    ball = Ball.defaultBall(true)
  }

  override def init(gc: GameContainer) {}

  override def update(gc: GameContainer, delta: Int) {
    paddleLeft = paddleLeft.move(paddleLeftVelocity)
    paddleRight = paddleRight.move(paddleRightVelocity)
    ball = ball.move
  }

  override def render(gc: GameContainer, g: Graphics) {

    g.drawString("Press R to Restart", 350, 300)
    g.drawString(leftScore.toString, 150, 50)
    g.drawString(rightScore.toString, 450, 50)

    g.drawLine(width / 2, 0, width / 2, height)
    g.drawLine(Paddle.Width, 0, Paddle.Width, height)
    g.drawLine(width - Paddle.Width, 0, width - Paddle.Width, height)

    ball.draw(g)
    paddleLeft.draw(g)
    paddleRight.draw(g)
  }
 
}
