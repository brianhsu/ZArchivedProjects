package org.bone.puyo

object Block
{
    import scala.util.Random

    val Wall  = Block("X")
    val Empty = Block(" ")

    val Red    = Block("R")
    val Green  = Block("G")
    val Blue   = Block("B")
    val Yellow = Block("Y")

    def random = {
        Random.nextInt(4) match {
            case 0 => Red
            case 1 => Green
            case 2 => Blue
            case 3 => Yellow
        }
    }
}

case class Block(id: String)
{
    override def toString = id
}


