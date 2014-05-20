package org.bone.puyo

object GameBoard
{
    import Block._

    val defaultField = List(
        Wall,  Wall,  Wall,  Wall,  Wall,  Wall,  Wall, Wall,
        Wall, Empty, Empty, Empty, Empty, Empty, Empty, Wall,
        Wall, Empty, Empty, Empty, Empty, Empty, Empty, Wall,
        Wall, Empty, Empty, Empty, Empty, Empty, Empty, Wall,
        Wall, Empty, Empty, Empty, Empty, Empty, Empty, Wall,
        Wall, Empty, Empty, Empty, Empty, Empty, Empty, Wall,
        Wall, Empty, Empty, Empty, Empty, Empty, Empty, Wall,
        Wall, Empty, Empty, Empty, Empty, Empty, Empty, Wall,
        Wall, Empty, Empty, Empty, Empty, Empty, Empty, Wall,
        Wall, Empty, Empty, Empty, Empty, Empty, Empty, Wall,
        Wall, Empty, Empty, Empty, Empty, Empty, Empty, Wall,
        Wall, Empty, Empty, Empty, Empty, Empty, Empty, Wall,
        Wall, Empty, Empty, Empty, Empty, Empty, Empty, Wall,
        Wall, Empty, Empty, Empty, Empty, Empty, Empty, Wall,
        Wall,  Wall,  Wall,  Wall,  Wall,  Wall,  Wall, Wall
    )
}

case class GameBoard(field: List[Block] = GameBoard.defaultField) extends GameAxis
{
    /**
     *  放置魔法氣泡到遊戲板面上
     *
     *  @param      要放置的魔法氣泡
     *  @return     放置氣泡後的遊戲面板
     */
    def placePuyo(puyo: Puyo) = {

        var newField = field

        puyo.nonEmptyBlocks.foreach { case(block, offset) =>
            val pos = calculateBlockPos(puyo, offset)

            newField = newField.updated(pos, block)
        }
        
        GameBoard(newField)
    }

    /**
     *  整理遊戲面板
     *
     *  將懸空的氣泡往下掉
     *
     *  @return     整理過後的遊戲面板
     */
    def compact() =
    {
        def indeces(column: Int) = (0 to 12).map( i => offsetY(column + 9, i)).toList

        var newField = field

        for (i <- 0 until 6) {
            val index = indeces(i)
            val nonEmpty  = index.map(pos => field(pos)).filter(_ != Block.Empty)
            val padding   = List.fill(index.size-nonEmpty.size)(Block.Empty)
            val newColumn = (padding ++ nonEmpty)

            index.zip(newColumn).foreach { case(pos, block) =>
                newField = newField.updated(pos, block)
            }
        }
        
        GameBoard(newField)
    }

    /**
     *  掃描連在一起續的同色氣泡
     *
     *  @param  targetBlock     氣泡種類
     *  @param  pos             目前的位置
     *  @param  posList         連接的同色氣泡的位置
     */
    private def scanTarget(targetBlock: Block, pos: Int, posList: List[Int]): List[Int] =
    {
        val currentBlock = field(pos)

        if (currentBlock != targetBlock || posList.contains(pos)) {
            return posList;
        }

        (scanTarget(targetBlock, offsetY(pos, -1), pos:: posList) ++
         scanTarget(targetBlock, offsetY(pos,  1), pos:: posList) ++
         scanTarget(targetBlock, offsetX(pos, -1), pos:: posList) ++
         scanTarget(targetBlock, offsetX(pos,  1), pos:: posList)).distinct
    }

    /**
     *  開始掃描某個位置上是否有四個以上的同色氣泡
     *
     *  @param  pos     要掃描的位置
     *  @return         同色氣泡的位置列表
     */
    def scan(pos: Int): List[Int] =
    {
        val currentBlock = field(pos)
        
        currentBlock match {
            case Block.Wall  => Nil
            case Block.Empty => Nil
            case block => 

                scanTarget(block, pos, Nil) match {
                    case xs if xs.size >= 4 => xs
                    case xs => Nil
                }
        }

    }

    /**
     *  消除面板上連續的同色魔法氣泡
     *
     *  @return 消除的過程
     *
     */
    def cleanup: GameBoard = {
        
        import Block._

        def clearBoard(clearTargets: List[Int]) = {
            var newField: List[Block] = field

            clearTargets.foreach{ pos => 
                newField = newField.updated(pos, Empty)
            }

            GameBoard(newField).compact.cleanup
        }

        val range = (9 to 110).filterNot(pos => field(pos) == Wall || field(pos) == Empty)
        val clearTargets = range.flatMap(pos => scan(pos)).toList

        clearTargets match {
            case Nil => this
            case xs  => clearBoard(clearTargets)
        }
    }

    def disableBoard = {
        
        var newField = field

        field.zipWithIndex.filter(_._1 != Block.Empty).foreach { case(block, pos) =>
            newField = newField.updated(pos, Block.Wall)
        }

        GameBoard(newField)
    }

    override def toString = {
        field.grouped(8).map(_.mkString).mkString("\n")
    }
}
