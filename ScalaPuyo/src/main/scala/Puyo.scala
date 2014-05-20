package org.bone.puyo

/**
 *  還沒被固定住的魔法氣泡
 *
 *  這個類別代表了畫面上玩家正在操控的，還沒被固定住的魔法氣泡，
 *  表示的方法為一個 3x3  的二維轉一維矩陣。
 *
 *  舉例來說，一個紅色和黃色的組成的泡泡，其初始表示如下：
 *
 *  OOO
 *  ORY
 *  OOO
 *
 *  當玩家旋轉氣泡時，以九宮格中間為中心，做逆時針方向的旋轉，
 *  所以這個氣泡旋轉後的可能結果如下：
 *
 *  OYO   OOO   OOO   OOO
 *  ORO   YRO   ORO   ORY
 *  OOO   OOO   OYO   OOO
 *
 *  @param  blocks  一個 3x3 = 9 的 Block，用一維陣列表示
 *  @param  pos     這個氣泡的右下角的最右下角的座標
 */
case class Puyo(blocks: List[Block], pos: Int = 12) extends GameAxis
{
    def left  = new Puyo(blocks, pos - 1)
    def right = new Puyo(blocks, pos + 1)
    def down  = new Puyo(blocks, pos + 8)

    /**
     *  旋轉泡泡
     *
     *  @return     旋轉後的魔法氣泡
     */
    def rotate = {

        // 取出除了中間的以外的氣泡種類和他在九九宮格內的座標
        val (block, index) = blocks.zipWithIndex.filter { case(block, index) => 
            index != 4 && block != Block.Empty
        }.last

        index match {
            case 5 => new Puyo(blocks.updated(5, Block.Empty).updated(1, block), pos) 
            case 1 => new Puyo(blocks.updated(1, Block.Empty).updated(3, block), pos) 
            case 3 => new Puyo(blocks.updated(3, Block.Empty).updated(7, block), pos)
            case 7 => new Puyo(blocks.updated(7, Block.Empty).updated(5, block), pos)
        }
    }

    /**
     *  取出魔法氣泡裡不是空白的泡泡和其在九宮格中的座標
     *
     *  @param  List[(泡泡種類, 九宮格內的座標)]
     */
    def nonEmptyBlocks = blocks.zipWithIndex.filter(_._1 != Block.Empty)

    override def toString = {
        blocks.grouped(3).map(_.mkString).mkString("\n")
    }
}

object Puyo
{
    /**
     *  隨機產生魔法氣泡
     */
    def randomNext = new Puyo(
        Block.Empty :: Block.Empty  :: Block.Empty  ::
        Block.Empty :: Block.random :: Block.random ::
        Block.Empty :: Block.Empty  :: Block.Empty  :: Nil
    )
}
