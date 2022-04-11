package chess

import java.lang.Math.abs

fun main() {
    val field = Board("Pawns-Only Chess")
    println("Pawns-Only Chess")
    println("First Player's name:")
    val firstPlayer = Player(readln() + "'s", Element.WHITE)
    println("Second Player's name:")
    val secondPlayer = Player(readln() + "'s", Element.BLACK)
    secondPlayer.kf = -1
    while (true) {
        field.draw()
        val str = field.promptMove(firstPlayer)
        if (str == "exit") {
            println("Bye!")
            return
        }
        field.draw()
        val str1 = field.promptMove(secondPlayer)
        if (str1 == "exit") {
            println("Bye!")
            return
        }
    }

}

enum class Element(val element: String) {
    EmptyCell(" "),
    DIVIDING("+---+---+---+---+---+---+---+---+"),
    ALPHABET("a   b   c   d   e   f   g   h"),
    WHITE("W"),
    BLACK("B")
}

data class Board(val title: String) {
    private val _board = Array(8) { Array(8) { Element.EmptyCell } }
    private val board
        get() = _board

    init {
        board[1].fill(Element.WHITE, 0, 8)
        board[6].fill(Element.BLACK, 0, 8)
    }

    private var initPoint = Move(0, 0)
    private var endPoint = Move(0, 0)
    private var enPass = ""
    private var enPassantMove = Move(0, 0)

    fun draw() {
        val boardAsString = board.mapIndexed { index, row ->
            val cells = row.joinToString(" | ") { it.element }
            "${index + 1} | $cells | \n  ${Element.DIVIDING.element}"
        }
        println("  ${Element.DIVIDING.element}")
        println(boardAsString.reversed().joinToString("\n"))
        println("    ${Element.ALPHABET.element}")
    }

    fun promptMove(player: Player): String {
        val reg = Regex("exit|[a-h][1-8][a-h][1-8]")
        while (true) {
            println("${player.name} turn:")
            val inputLine = readln()
            when {
                inputLine == "exit" -> {
                    return "exit"
                }
                !inputLine.matches(reg) -> {
                    println("Invalid Input")

                }
                isOrNot(inputLine, player) -> {
                }
                !cellMove(player) -> {
                    println("Invalid Input")
                }
                else -> {
                    move(initPoint, endPoint, player.side)
                    return ""
                }
            }
        }
    }

    private fun move(start: Move, end: Move, side: Element) {
        board[start.y][start.x] = Element.EmptyCell
        board[end.y][end.x] = side
    }

    private fun isFinalFree(): Boolean {
        return board[endPoint.y][endPoint.x] == Element.EmptyCell
    }

    private fun cellMove(player: Player): Boolean {
        if (doOrNotEnPass(player)) {
            return true
        } else if (endPoint.x == initPoint.x) {
            return if (isFinalFree() && endPoint.x == initPoint.x) {
                when {
                    endPoint.y - initPoint.y == 2 * player.kf && initPoint.y == 1 &&
                            board[endPoint.y - 1][endPoint.x] == Element.EmptyCell ||
                            endPoint.y - initPoint.y == 1 * player.kf ||
                            endPoint.y - initPoint.y == 2 * player.kf && initPoint.y == 6 &&
                            board[endPoint.y + 1][endPoint.x] == Element.EmptyCell -> {
                        defineEnPass(player)
                        true
                    }
                    else -> false
                }
            } else false
        } else if (player.side.element != board[endPoint.y][endPoint.x].element &&
            board[endPoint.y][endPoint.x].element != Element.EmptyCell.element
        ) { var variantMove = 0
            cycle@ for (pair in pawnTakeMove) {
//                println("cycling")
//                println("${endPoint.y} ${initPoint.y + pair.y * player.kf}")
//                println("${endPoint.x} ${initPoint.x + pair.x * player.kf}")
                if (endPoint.y == (initPoint.y + pair.y * player.kf) &&
                    endPoint.x == (initPoint.x + pair.x * player.kf)
                ) {
//                    println("breaking cycle with ${endPoint.y} ${endPoint.x}")
                    variantMove++
                    break@cycle
                }
            }
            return variantMove > 0
        }
        return false
    }

    private fun doOrNotEnPass(player: Player): Boolean {
        if (enPassantMove.y == 0) {
            return false
        } else {
            if ((enPassantMove.y == endPoint.y) && (enPassantMove.x == endPoint.x) &&
                abs(endPoint.x - initPoint.x) == 1) {
                board[endPoint.y - 1 * player.kf][endPoint.x] = Element.EmptyCell
                enPass = ""
                enPassantMove = Move(0, 0)
                return true
            }
            enPass = ""
            enPassantMove = Move(0, 0)
            return false
        }
    }

    private fun defineEnPass(player: Player) {
        if (endPoint.y - initPoint.y == 2 * player.kf) {
            enPassantMove = Move(endPoint.x, endPoint.y - 1 * player.kf)
            enPass = player.side.element
        }
    }

    private fun isOrNot(inputLine: String, player: Player): Boolean {
        transformMove(inputLine)

        if (board[initPoint.y][initPoint.x] == player.side) {
            return false
        }
        println("No ${(player.side).toString().lowercase()} pawn at ${inputLine[0]}${inputLine[1]}")
        return true
    }

    private fun transformMove(stringMove: String) {
        initPoint = Move(stringMove[0].code - 97, stringMove[1].digitToInt() - 1)
        endPoint = Move(stringMove[2].code - 97, stringMove[3].digitToInt() - 1)
    }

    companion object {
        private val pawnTakeMove = listOf(Move(1, 1), Move(-1, 1))
    }
}

class Player(_name: String, colour: Element) {
    var side = colour
    var name = _name
    var kf: Int = 1
}

class Move(_x: Int, _y: Int) {
    val x = _x
    val y = _y
}
