package org.bone.puyo

class GameStage extends GameAxis
{
    var board = new GameBoard
    var currentPuyo = Puyo.randomNext

    def isGameOver = board.field(11) != Block.Empty || board.field(12) != Block.Empty

    def isValidMove(puyo: Puyo) = {
        puyo.nonEmptyBlocks.forall{ case(block, pos) => 
            board.field(calculateBlockPos(puyo, pos)) == Block.Empty
        }
    }

    def left() = {
        currentPuyo = currentPuyo.left match {
            case puyo if isValidMove(puyo) => puyo
            case _ => currentPuyo
        }
    }

    def right() = {
        currentPuyo = currentPuyo.right match {
            case puyo if isValidMove(puyo) => puyo
            case _ => currentPuyo
        }
    }

    def rotate() = {
        currentPuyo = currentPuyo.rotate match {
            case puyo if currentPuyo.pos <= 22 => currentPuyo
            case puyo if isValidMove(puyo) => puyo
            case puyo if isValidMove(puyo.left) => puyo.left
            case puyo if isValidMove(puyo.right) => puyo.right
            case puyo => currentPuyo
        }
    }


    def down() = {
        val oldPuyo = currentPuyo
        currentPuyo = currentPuyo.down match {
            case puyo if isValidMove(puyo) => puyo
            case _ => currentPuyo
        }

        if (oldPuyo == currentPuyo) {
            isGameOver match {
                case true =>
                    this.board = board.disableBoard
                    this.currentPuyo = Puyo.randomNext

                case false =>
                    this.board = board.placePuyo(currentPuyo).compact
                    this.currentPuyo = Puyo.randomNext
                    this.board = board.cleanup
            }
        }
    }

    def startGame()
    {
        val thread = new Thread {
            override def run ()
            {
                while(true && !isGameOver) {
                    GameStage.this.down()
                    Thread.sleep(1000)
                }
            }
        }

        thread.start()
    }


}

