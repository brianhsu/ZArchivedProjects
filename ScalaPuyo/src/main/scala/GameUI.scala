package org.bone.puyo

import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11._


object GameUI extends GameAxis
{
    var stage = new GameStage

    def initOpenGL()
    {
        glColor3f(0.0f, 1.0f, 0.0f)
        glPointSize(20)

        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        glOrtho(0, 300, 400, 0, 1, -1)
    }

    def updateScreen()
    {
        glClear(GL_COLOR_BUFFER_BIT)

        drawBoard(stage.board)

        if (!stage.isGameOver) {
            drawPuyo(stage.currentPuyo)
        }
    }

    def drawPuyo(puyo: Puyo)
    {
        puyo.nonEmptyBlocks.foreach { case(block, offset) =>
            drawBlock(block, calculateBlockPos(puyo, offset))
        }
    }

    def drawBlock(block: Block, pos: Int)
    {
        val (x, y) = to2D(pos)

        block match {
            case Block.Wall   => glColor3f(0.8f, 0.8f, 0.8f)
            case Block.Red    => glColor3f(1.0f, 0.0f, 0.0f)
            case Block.Green  => glColor3f(0.0f, 1.0f, 0.0f)
            case Block.Blue   => glColor3f(0.0f, 0.0f, 1.0f)
            case Block.Yellow => glColor3f(1.0f, 1.0f, 0.0f)
        }

        glBegin(GL_POINTS)
            glVertex2i(x * 20, y * 20)
        glEnd()
        glFlush()
    }

    def drawBoard(board: GameBoard)
    {
        board.field.zipWithIndex.filter(_._1 != Block.Empty).foreach { case(block, pos) => 
            drawBlock(block, pos)
        }
    }

    def start()
    {
        Display.setDisplayMode(new DisplayMode(300, 400))
        Display.setTitle("魔法氣泡")
        Display.create()
        Keyboard.enableRepeatEvents(true)

        initOpenGL()
        updateScreen()
        stage.startGame()

        while (!Display.isCloseRequested()) {
            processKeyEvent()
            updateScreen()
            Display.update()
        }
 
        Display.destroy()
    }

    def processKeyEvent()
    {
        while(Keyboard.next) {
            val state = Keyboard.getEventKeyState
            val key = Keyboard.getEventKey

            (key, state) match {
                case (Keyboard.KEY_UP, true)    => stage.rotate
                case (Keyboard.KEY_DOWN, true)  => stage.down
                case (Keyboard.KEY_LEFT, true)  => stage.left
                case (Keyboard.KEY_RIGHT, true) => stage.right
                case (Keyboard.KEY_Q, true) => sys.exit()
                case (Keyboard.KEY_RETURN, true) if stage.isGameOver =>
                    this.stage = new GameStage
                    this.stage.startGame()
                    updateScreen()
                    
                case _ =>
            }

            updateScreen()
        }
    }
    
    def main(args: Array[String])
    {
        start()
    }
}


