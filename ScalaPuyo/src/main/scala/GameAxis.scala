package org.bone.puyo

trait GameAxis
{
    val width  = 8    // 含牆壁的遊戲板面寬度
    val height = 15   // 含牆壁的遊戲板面高度

    private val offsetMapping = Array(-18, -17, -16, -10, -9, -8, -2, -1, 0)

    def calculateBlockPos(puyo: Puyo, offset: Int) = (puyo.pos) + offsetMapping(offset)

    def offsetX(currentPos: Int, x: Int) = (currentPos + x) % (width * height)
    def offsetY(currentPos: Int, y: Int) = (currentPos + y * width) % (width * height)

    def to2D(pos: Int) = (pos % width, pos / width)
}

