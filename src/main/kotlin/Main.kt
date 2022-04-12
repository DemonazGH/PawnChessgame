package chess

import java.util.*
import kotlin.system.exitProcess

fun main() {
    gameStart()
}

fun gameStart() {
    val field = Board("Pawns-Only Chess")
    println("Pawns-Only Chess")
    println("First Player's name:")
    val firstPlayer = Player(readln() + "'s", Element.WHITE, 1)
    println("Second Player's name:")
    val secondPlayer = Player(readln() + "'s", Element.BLACK, -1)
    field.gameInit(firstPlayer, secondPlayer)
}

data class Board(val title: String) {
    private val _board = Array(8) { Array(8) { Element.EmptyCell } }
    private val board
        get() = _board

    init {
 //       board[1].fill(Element.WHITE, 0, 8)
        board[1][1] = Element.WHITE
        board[6].fill(Element.BLACK, 0, 8)
    }

    private var whiteOnField = 0
    private var blackOnField = 0
    private var turn = 0
    private var winFlag = ""
    private var initPoint = Move(0, 0)
    private var endPoint = Move(0, 0)
    private var enPass = ""
    private var enPassantMove = Move(0, 0)


    private fun draw() {
        val boardAsString = board.mapIndexed { index, row ->
            val cells = row.joinToString(" | ") { it.element }
            "${index + 1} | $cells | \n  ${Element.DIVIDING.element}"
        }
        println("  ${Element.DIVIDING.element}")
        println(boardAsString.reversed().joinToString("\n"))
        println("    ${Element.ALPHABET.element}")
    }

    fun gameInit(firstPlayer: Player, secondPlayer: Player) {

        while (true) {
            println("WHITE - $whiteOnField black - $blackOnField")
            println("turn is $turn")
            staleMate(if (turn == 0) firstPlayer else secondPlayer)
            draw()
            chkWin()
            if (turn == 0) promptMove(firstPlayer) else promptMove(secondPlayer)
        }
    }


    private fun promptMove(player: Player): String {
        val reg = Regex("exit|[a-h][1-8][a-h][1-8]")
        while (true) {
            println("${player.name} turn:")
            val inputLine = readln()
            when {
                inputLine == "exit" -> {
                    println("Bye!")
                    exitProcess(0)
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
                    move(initPoint, endPoint, player)
                    return ""
                }
            }
        }
    }

    private fun move(start: Move, end: Move, player: Player) {
        board[start.y][start.x] = Element.EmptyCell
        board[end.y][end.x] = player.side
        println("move is ${end.y} ${end.x}")
        turn = if (turn == 0) 1 else 0
        if (end.y == 7 || end.y == 0) {
            winFlag = player.side.name
        }
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
            /* check if destination cell is empty or has rival's pawn */
        } else if (player.side.element != board[endPoint.y][endPoint.x].element &&
            board[endPoint.y][endPoint.x].element != Element.EmptyCell.element
        ) {
            var variantMove = 0
            cycle@ for (pair in pawnTakeMove) {

                if (endPoint.y == (initPoint.y + pair.y * player.kf) &&
                    endPoint.x == (initPoint.x + pair.x * player.kf)
                ) {
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
                kotlin.math.abs(endPoint.x - initPoint.x) == 1
            ) {
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

    private fun chkWin() {
        whiteOnField = 0
        blackOnField = 0
        for (entry in board) {
            entry.forEach {
                if (it == Element.WHITE) {
                    whiteOnField++
                } else if (it == Element.BLACK) {
                    blackOnField++
                }
            }
        }
        if (whiteOnField == 0) {
            winFlag = "Black"
        } else if (blackOnField == 0) winFlag = "white"
        if (winFlag.isNotEmpty()) {
            println("${
                winFlag.lowercase(Locale.getDefault())
                    .replaceFirstChar {
                        if (it.isLowerCase()) it
                            .titlecase(Locale.getDefault()) else it.toString()
                    }
            } Wins!"
            )
            println("Bye!")
            exitProcess(0)
        }
    }

    fun staleMate(player: Player) {
        println("Processing stalemate for ${player.side}")
        val pawnsList = mutableListOf<Move>()
        for (y in 0..7) {
            for (x in 0..7) {
                if (board[y][x] == player.side) {
//                    println("y = $y x = $x")
                    pawnsList.add(Move(x,y))
                }
            }
        }
 //       println(pawnsList.forEach { println("${it.y} ${it.x}") })
        var result = mutableListOf<Move>()
        var y = 0
        var x = 0
        for (pair in pawnsList) {
            for (cords in pawnTakeMove ) {
 //               println("${pair.y} ${cords.y} ${pair.x} ${cords.x}")
                try {
                if (board[pair.y + 1 * player.kf][pair.x] == Element.EmptyCell) {
                    println("available")
                    result.add(pair)
                } else {
                    println("unavail")
                }
             } catch (_: ArrayIndexOutOfBoundsException) {}
            }
        }
        println("after stalemate")
        if (result.isEmpty()){
            println("Stelemate!\nBye!")
            exitProcess(0)
         }
        result.forEach { println("${it.y} ${it.x}") }
    }


    companion object {
        private val pawnTakeMove = listOf(Move(1, 1), Move(-1, 1))
    }

}

class Move(_x: Int, _y: Int) {
    val x = _x
    val y = _y
}

enum class Element(val element: String) {
    EmptyCell(" "),
    DIVIDING("+---+---+---+---+---+---+---+---+"),
    ALPHABET("a   b   c   d   e   f   g   h"),
    WHITE("W"),
    BLACK("B")
}

class Player(_name: String, colour: Element, kv: Int) {
    var side = colour
    var name = _name
    var kf = kv
}
