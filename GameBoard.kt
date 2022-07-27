package base.base2.minesweeperandroid

import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import base.example.minesweeperandroid.R
import kotlin.math.max
import kotlin.math.min


enum class GameState {
    PLAYING,
    WON,
    LOST
}


class GameBoard() {
    var gameState: GameState = GameState.PLAYING

    var correctlyFoundBombs: Int = 0
    var width: Int = 0
    var height: Int = 0
    var bombCount: Int = 0
    var flagCount: Int = 0
    var board: MutableList<MutableList<FieldButton>> = mutableListOf()
    var botoane: MutableList<FieldButton> = mutableListOf()

    var parent: ConstraintLayout? = null

    lateinit var bombCountText: TextView
    lateinit var smiley: Button
    lateinit var bombSetterField: EditText
    lateinit var bombSetterLabel: TextView

    lateinit var widthSetterField: EditText
    lateinit var widthSetterLabel: TextView

    lateinit var heightSetterField: EditText
    lateinit var heightSetterLabel: TextView

    constructor(context: Context, parent: ConstraintLayout, width: Int, height: Int, bombCount: Int) : this() {

        //initialize board
        this.width = width
        this.height = height
        this.bombCount = bombCount
        this.parent = parent

        //region create game board

        //region allocate board
        allocateBoard(context)
        //endregion

        //generate bombs
        generateBombTiles()

        //set coordinates for all buttons
        setButtonCoordinates(context)

        //endregion

        //region create smiley

        val displayWidth = context.resources.displayMetrics.widthPixels
        val displayHeight = context.resources.displayMetrics.heightPixels

        val minDisplaySize = min(displayWidth, displayHeight)
        val maxDisplaySize = max(displayWidth, displayHeight)

        val minBoardSize = min(width, height)
        val maxBoardSize = max(width, height)

        val usedSize = minDisplaySize / 10
        smiley = Button(context)
        smiley.layoutParams = ConstraintLayout.LayoutParams(usedSize * 2, usedSize * 2)
        //position the smiley above the board
        smiley.x = displayWidth / 2 - usedSize.toFloat()
        smiley.y = usedSize * 1.5f

        smiley.setBackgroundResource(R.drawable.smiley)
        parent.addView(smiley)
        //when the smiley is pressed, reset the game board
        smiley.setOnClickListener {
            resetGameBoard()
        }

        //endregion

        //region create UI and settings button

        //region create bombCounter
        bombCountText = TextView(context)
        bombCountText.layoutParams = ConstraintLayout.LayoutParams(usedSize * 2, usedSize * 2)
        bombCountText.x = usedSize * 1.5f
        bombCountText.y = usedSize * 1.5f
        bombCountText.text = "Bomb count: " + bombCount.toString()
        bombCountText.textSize = usedSize / 6f
        bombCountText.setBackgroundResource(0)
        bombCountText.isClickable = false
        //set the size of the box to fit the text
        bombCountText.width = bombCountText.paint.measureText(bombCountText.text.toString()).toInt()
        bombCountText.height = bombCountText.paint.fontMetricsInt.bottom - bombCountText.paint.fontMetricsInt.top
        parent.addView(bombCountText)
        //endregion

        //region create bombSetterField
        //create an edittext field to set the number of bombs
        bombSetterField = EditText(context)
        bombSetterField.layoutParams = ConstraintLayout.LayoutParams(usedSize * 2, usedSize)
        bombSetterField.x = usedSize * 7f
        bombSetterField.y = (displayHeight - usedSize * 1.5f).toFloat()
        bombSetterField.textSize = usedSize / 6f
        bombSetterField.setBackgroundColor(Color.BLACK)
        //only allow digits to be entered
        bombSetterField.inputType = InputType.TYPE_CLASS_NUMBER
        bombSetterField.visibility = View.INVISIBLE
        parent.addView(bombSetterField)

        //when the user presses enter, set the bomb count to the value in the field
        bombSetterField.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                //if the value is not a number, do nothing
                if (bombSetterField.text.toString().toIntOrNull() == null) {
                    return@setOnKeyListener false
                }

                this.bombCount = clamp(bombSetterField.text.toString().toInt(), 0, this.width*this.height - 1)

                bombCountText.setText(bombCount.toString())
                bombCountText.width = bombCountText.paint.measureText(bombCountText.text.toString()).toInt()
                bombCountText.height = bombCountText.paint.fontMetricsInt.bottom - bombCountText.paint.fontMetricsInt.top
                bombSetterField.width = bombSetterField.paint.measureText(bombSetterField.text.toString()).toInt()
                bombSetterField.height = bombSetterField.paint.fontMetricsInt.bottom - bombSetterField.paint.fontMetricsInt.top

                resetGameBoard()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        //endregion

        //region create bombSetterLabel
        //create a label that says "New bomb count:" to the right of the settings button and that can't be clicked and has no background
        bombSetterLabel = TextView(context)
        bombSetterLabel.layoutParams = ConstraintLayout.LayoutParams(usedSize * 5, usedSize)
        bombSetterLabel.x = usedSize * 3f
        bombSetterLabel.y = (displayHeight - usedSize * 1.3f).toFloat()
        bombSetterLabel.text = "New Bomb count:"
        bombSetterLabel.textSize = usedSize / 6f
        bombSetterLabel.setBackgroundResource(0)
        bombSetterLabel.isClickable = false
        bombSetterLabel.visibility = View.INVISIBLE
        parent.addView(bombSetterLabel)
        //endregion

        //region create widthSetterField
        //create an edittext field to set the width of the board
        widthSetterField = EditText(context)
        widthSetterField.layoutParams = ConstraintLayout.LayoutParams(usedSize * 2, usedSize)
        widthSetterField.x = usedSize * 7f
        widthSetterField.y = (displayHeight - usedSize * 2.5f).toFloat()
        widthSetterField.textSize = usedSize / 6f
        widthSetterField.setBackgroundColor(Color.BLACK)
        //only allow digits to be entered
        widthSetterField.inputType = InputType.TYPE_CLASS_NUMBER
        widthSetterField.visibility = View.INVISIBLE
        parent.addView(widthSetterField)

        //when the user presses enter, set the bomb count to the value in the field
        widthSetterField.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                //if the value is not a number, do nothing
                if (widthSetterField.text.toString().toIntOrNull() == null) {
                    return@setOnKeyListener false
                }

                this.width = clamp(widthSetterField.text.toString().toInt(), 1, 50)

                widthSetterField.setText(this.width.toString())
                widthSetterField.width = widthSetterField.paint.measureText(widthSetterField.text.toString()).toInt()
                widthSetterField.height = widthSetterField.paint.fontMetricsInt.bottom - widthSetterField.paint.fontMetricsInt.top
                widthSetterField.width = widthSetterField.paint.measureText(widthSetterField.text.toString()).toInt()
                widthSetterField.height = widthSetterField.paint.fontMetricsInt.bottom - widthSetterField.paint.fontMetricsInt.top

                if (this.height * this.width < this.bombCount) {
                    this.bombCount = this.height * this.width - 1
                }

                deleteBoard()
                allocateBoard(context)
                setButtonCoordinates(context)
                resetGameBoard()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        //endregion

        //region create widthSetterLabel
        //create a label that says "New bomb count:" to the right of the settings button and that can't be clicked and has no background
        widthSetterLabel = TextView(context)
        widthSetterLabel.layoutParams = ConstraintLayout.LayoutParams(usedSize * 5, usedSize)
        widthSetterLabel.x = usedSize * 3f
        widthSetterLabel.y = (displayHeight - usedSize * 2.3f).toFloat()
        widthSetterLabel.text = "New width:"
        widthSetterLabel.textSize = usedSize / 6f
        widthSetterLabel.setBackgroundResource(0)
        widthSetterLabel.isClickable = false
        widthSetterLabel.visibility = View.INVISIBLE
        parent.addView(widthSetterLabel)
        //endregion


        //region create heightSetterField
        //create an edittext field to set the width of the board
        heightSetterField = EditText(context)
        heightSetterField.layoutParams = ConstraintLayout.LayoutParams(usedSize * 2, usedSize)
        heightSetterField.x = usedSize * 7f
        heightSetterField.y = (displayHeight - usedSize * 3.5f).toFloat()
        heightSetterField.textSize = usedSize / 6f
        heightSetterField.setBackgroundColor(Color.BLACK)
        //only allow digits to be entered
        heightSetterField.inputType = InputType.TYPE_CLASS_NUMBER
        heightSetterField.visibility = View.INVISIBLE
        parent.addView(heightSetterField)

        //when the user presses enter, set the bomb count to the value in the field
        heightSetterField.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                //if the value is not a number, do nothing
                if (heightSetterField.text.toString().toIntOrNull() == null) {
                    return@setOnKeyListener false
                }

                this.height = clamp(heightSetterField.text.toString().toInt(), 1, 50)

                heightSetterField.setText(this.height.toString())
                heightSetterField.width = heightSetterField.paint.measureText(heightSetterField.text.toString()).toInt()
                heightSetterField.height = heightSetterField.paint.fontMetricsInt.bottom - heightSetterField.paint.fontMetricsInt.top
                heightSetterField.width = heightSetterField.paint.measureText(heightSetterField.text.toString()).toInt()
                heightSetterField.height = heightSetterField.paint.fontMetricsInt.bottom - heightSetterField.paint.fontMetricsInt.top

                if (this.height * this.width < this.bombCount) {
                    this.bombCount = this.height * this.width - 1
                }

                deleteBoard()
                allocateBoard(context)
                setButtonCoordinates(context)
                resetGameBoard()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        //endregion

        //region create heightSetterLabel
        //create a label that says "New bomb count:" to the right of the settings button and that can't be clicked and has no background
        heightSetterLabel = TextView(context)
        heightSetterLabel.layoutParams = ConstraintLayout.LayoutParams(usedSize * 5, usedSize)
        heightSetterLabel.x = usedSize * 3f
        heightSetterLabel.y = (displayHeight - usedSize * 3.3f).toFloat()
        heightSetterLabel.text = "New height:"
        heightSetterLabel.textSize = usedSize / 6f
        heightSetterLabel.setBackgroundResource(0)
        heightSetterLabel.isClickable = false
        heightSetterLabel.visibility = View.INVISIBLE
        parent.addView(heightSetterLabel)
        //endregion


        //region create settings button
        //create a button to show the settings and place it bellow the board, in the right corner
        val settingsButton = Button(context)
        settingsButton.layoutParams = ConstraintLayout.LayoutParams(usedSize * 2, usedSize * 2)
        settingsButton.x = 0f
        settingsButton.y = displayHeight - usedSize * 2f
        settingsButton.setBackgroundResource(R.drawable.settings)
        //when the button is pressed, show or hide the bombSetterField
        settingsButton.setOnClickListener {
            if (bombSetterField.visibility == View.VISIBLE) {
                bombSetterLabel.visibility = View.INVISIBLE
                bombSetterField.visibility = View.INVISIBLE

                widthSetterField.visibility = View.INVISIBLE
                widthSetterLabel.visibility = View.INVISIBLE

                heightSetterField.visibility = View.INVISIBLE
                heightSetterLabel.visibility = View.INVISIBLE
            } else {
                bombSetterLabel.visibility = View.VISIBLE
                bombSetterField.visibility = View.VISIBLE

                widthSetterField.visibility = View.VISIBLE
                widthSetterLabel.visibility = View.VISIBLE

                heightSetterField.visibility = View.VISIBLE
                heightSetterLabel.visibility = View.VISIBLE
            }
        }
        parent.addView(settingsButton)
        //endregion

        //endregion
    }

    fun setButtonCoordinates(context:Context){

        val displayWidth = context.resources.displayMetrics.widthPixels
        val displayHeight = context.resources.displayMetrics.heightPixels

        val minDisplaySize = min(displayWidth, displayHeight)
        val maxDisplaySize = max(displayWidth, displayHeight)

        val minBoardSize = min(width, height)
        val maxBoardSize = max(width, height)

        //TODO: Find a better formula for this
        var usedSize = minDisplaySize / maxBoardSize

        for(i in 0 until height)
            for(j in 0 until width) {
                val button = board[i][j]
                button.layoutParams = ConstraintLayout.LayoutParams(usedSize, usedSize)

                //set coordinates while centering the board
                button.x = ((displayWidth - usedSize * width) / 2 + j * usedSize).toFloat()
                button.y = ((displayHeight - usedSize * height) / 2 + i * usedSize).toFloat()

                //give the button the image with the name "tile"
                button.setBackgroundResource(R.drawable.tile_unpressed)
                //add the button to the board
                parent!!.addView(button)

            }
    }

    fun deleteBoard(){
        for(i in this.botoane)
            i.visibility = View.GONE

        botoane.clear()
        board.clear()
    }

    fun allocateBoard(context:Context){

        //initialize board
        for (i in 0 until height) {
            board.add(mutableListOf())
            for (j in 0 until width) {
                val boton = FieldButton(context, this)
                board[i].add(boton)
                board[i][j].coords = Pair(i, j)

                botoane.add(boton)
            }
        }
    }

    fun generateBombTiles(){

        //add all coords to a temp list
        val tempArray: MutableList<Pair<Int, Int>> = mutableListOf()
        for(i in 0 until height)
            for(j in 0 until width) {
                tempArray.add(Pair(i, j))
            }

        //randomly select bomb coords
        for(i in 0 until bombCount) {
            val randomIndex = (0 until tempArray.size).random()
            val randomPair = tempArray[randomIndex]
            val randomButton = board[randomPair.first][randomPair.second]
            randomButton.hasBomb = true
            tempArray.removeAt(randomIndex)
        }

        //set numbers for all non-bomb buttons avoiding the edge cases
        for(i in 0 until height) {
            for(j in 0 until width) {
                val button = board[i][j]
                if(!button.hasBomb) {
                    var count = 0
                    if(i > 0 && j > 0 && board[i-1][j-1].hasBomb) count++
                    if(i > 0 && board[i-1][j].hasBomb) count++
                    if(i > 0 && j < width-1 && board[i-1][j+1].hasBomb) count++
                    if(j > 0 && board[i][j-1].hasBomb) count++
                    if(j < width-1 && board[i][j+1].hasBomb) count++
                    if(i < height-1 && j > 0 && board[i+1][j-1].hasBomb) count++
                    if(i < height-1 && board[i+1][j].hasBomb) count++
                    if(i < height-1 && j < width-1 && board[i+1][j+1].hasBomb) count++
                    button.nearbyBombs = count
                }
            }
        }
    }

    fun clamp(value:Int, min:Int, max:Int): Int{
        var copy:Int = value
        if(value < min) copy = min
        if(value > max) copy = max
        return copy
    }

    fun revealedAOE(coords: Pair<Int, Int>){
        //trigger the neighbors of the given coords avoiding the edge cases
        val i = coords.first
        val j = coords.second

        if(i > 0 && j > 0 && !board[i-1][j-1].hasFlag)
            triggerTile(Pair(i-1, j-1))
        if(i > 0 && !board[i-1][j].hasFlag)
            triggerTile(Pair(i-1, j))
        if(i > 0 && j < width-1 && !board[i-1][j+1].hasFlag)
            triggerTile(Pair(i-1, j+1))
        if(j > 0 && !board[i][j-1].hasFlag)
            triggerTile(Pair(i, j-1))
        if(j < width-1 && !board[i][j+1].hasFlag)
            triggerTile(Pair(i, j+1))
        if(i < height-1 && j > 0 && !board[i+1][j-1].hasFlag)
            triggerTile(Pair(i+1, j-1))
        if(i < height-1 && !board[i+1][j].hasFlag)
            triggerTile(Pair(i+1, j))
        if(i < height-1 && j < width-1 && !board[i+1][j+1].hasFlag)
            triggerTile(Pair(i+1, j+1))

    }

    fun resetGameBoard() {
        smiley.setBackgroundResource(R.drawable.smiley)
        //reset the game board
        for(i in 0 until height)
            for(j in 0 until width) {
                val button = board[i][j]
                button.setBackgroundResource(R.drawable.tile_unpressed)
                button.isRevealed = false
                button.hasFlag = false
                button.hasBomb = false
                button.nearbyBombs = 0
            }
        flagCount = 0
        correctlyFoundBombs = 0
        bombCountText.text = "Bomb count: " + bombCount.toString()
        //reset the game state
        generateBombTiles()
        gameState = GameState.PLAYING
    }

    fun triggerTile(coords: Pair<Int, Int>){

        var cnt = 0
        val tileList: MutableList<FieldButton> = mutableListOf()
        var button = board[coords.first][coords.second]

        //add the selected button to the list
        tileList.add(button)

        while(tileList.isNotEmpty()){
            button = tileList[0]
            button.triggerTile()

            if(button.nearbyBombs == 0) {
                //add all neighbors to the list
                if(button.coords.first > 0 && button.coords.second > 0 && !board[button.coords.first - 1][button.coords.second - 1].isRevealed && !board[button.coords.first - 1][button.coords.second - 1].hasBomb && !board[button.coords.first - 1][button.coords.second - 1].hasFlag)
                    if(!tileList.contains(board[button.coords.first - 1][button.coords.second - 1]))
                        tileList.add(board[button.coords.first - 1][button.coords.second - 1])

                if(button.coords.first > 0 && !board[button.coords.first - 1][button.coords.second].isRevealed && !board[button.coords.first - 1][button.coords.second].hasBomb && !board[button.coords.first - 1][button.coords.second].hasFlag)
                    if(!tileList.contains(board[button.coords.first - 1][button.coords.second]))
                        tileList.add(board[button.coords.first - 1][button.coords.second])

                if(button.coords.first > 0 && button.coords.second < width-1 && !board[button.coords.first - 1][button.coords.second + 1].isRevealed && !board[button.coords.first - 1][button.coords.second + 1].hasBomb && !board[button.coords.first - 1][button.coords.second + 1].hasFlag)
                    if(!tileList.contains(board[button.coords.first - 1][button.coords.second + 1]))
                        tileList.add(board[button.coords.first - 1][button.coords.second + 1])

                if(button.coords.second > 0 && !board[button.coords.first][button.coords.second - 1].isRevealed && !board[button.coords.first][button.coords.second - 1].hasBomb && !board[button.coords.first][button.coords.second - 1].hasFlag)
                    if(!tileList.contains(board[button.coords.first][button.coords.second - 1]))
                        tileList.add(board[button.coords.first][button.coords.second - 1])

                if(button.coords.second < width-1 && !board[button.coords.first][button.coords.second + 1].isRevealed && !board[button.coords.first][button.coords.second + 1].hasBomb && !board[button.coords.first][button.coords.second + 1].hasFlag)
                    if(!tileList.contains(board[button.coords.first][button.coords.second + 1]))
                        tileList.add(board[button.coords.first][button.coords.second + 1])

                if(button.coords.first < height-1 && button.coords.second > 0 && !board[button.coords.first + 1][button.coords.second - 1].hasFlag)
                    if(!board[button.coords.first + 1][button.coords.second - 1].isRevealed && !board[button.coords.first + 1][button.coords.second - 1].hasBomb)
                        if(!tileList.contains(board[button.coords.first + 1][button.coords.second - 1]))
                            tileList.add(board[button.coords.first + 1][button.coords.second - 1])

                if(button.coords.first < height-1 && !board[button.coords.first + 1][button.coords.second].isRevealed && !board[button.coords.first + 1][button.coords.second].hasBomb && !board[button.coords.first + 1][button.coords.second].hasFlag)
                    if(!tileList.contains(board[button.coords.first + 1][button.coords.second]))
                        tileList.add(board[button.coords.first + 1][button.coords.second])

                if(button.coords.first < height-1 && button.coords.second < width-1 && !board[button.coords.first + 1][button.coords.second + 1].isRevealed && !board[button.coords.first + 1][button.coords.second + 1].hasBomb && !board[button.coords.first + 1][button.coords.second + 1].hasFlag)
                    if(!tileList.contains(board[button.coords.first + 1][button.coords.second + 1]))
                        tileList.add(board[button.coords.first + 1][button.coords.second + 1])

            }


            //remove the first button from the list
            tileList.removeAt(0)

        }


    }

    fun revealAll(){
        gameState = GameState.LOST
        for(i in 0..height-1)
            for(j in 0..width-1)
                board[i][j].triggerTile()
    }

    fun revealUnflagged(){
        gameState = GameState.LOST
        for(i in 0..height-1)
            for(j in 0..width-1)
                if (!board[i][j].hasFlag)
                    board[i][j].triggerTile()
    }
}