package base.base2.minesweeperandroid

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import base.example.minesweeperandroid.R

class FieldButton : AppCompatButton {

    //fields
    var hasBomb = false
    var nearbyBombs = 0
    var hasFlag = false
    var isRevealed = false
    var coords: Pair<Int, Int> = Pair(0, 0)

    var parent: GameBoard? = null

    //constructors
    constructor(context: Context, parent: GameBoard) : super(context){ init(); this.parent = parent }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { init() }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init() }

    private fun init() {
        isRevealed = false
        this.setBackgroundResource(R.drawable.tile_unpressed)
        makeCustomGestureDetector()
    }

    //functions
    fun triggerTile(){
        if(!isRevealed){

            if(hasBomb) {
                //daca treaba se intampla fix cand mori
                if(parent!!.gameState == GameState.PLAYING) {
                    parent!!.revealAll()
                    parent!!.smiley.setBackgroundResource(R.drawable.smiley_dead)
                    this.setBackgroundResource(R.drawable.tile_bomb_detonated)
                }
                else
                    //daca treaba se intampla cand isi dau reveal toate
                    if(!hasFlag)
                        this.setBackgroundResource(R.drawable.tile_bomb_not_detonated)

                this.isRevealed = true
                return
            }
            else if(hasFlag && parent!!.gameState == GameState.LOST) {
                this.setBackgroundResource(R.drawable.tile_bomb_wrong)
                this.isRevealed = true
                return
            }

            when(nearbyBombs){
                0 -> {
                    this.setBackgroundResource(R.drawable.tile_pressed)
                    this.isRevealed = true
                }
                1 -> {
                    this.setBackgroundResource(R.drawable.tile1)
                    this.isRevealed = true
                }
                2 -> {
                    this.setBackgroundResource(R.drawable.tile2)
                    this.isRevealed = true
                }
                3 -> {
                    this.setBackgroundResource(R.drawable.tile3)
                    this.isRevealed = true
                }
                4 -> {
                    this.setBackgroundResource(R.drawable.tile4)
                    this.isRevealed = true
                }
                5 -> {
                    this.setBackgroundResource(R.drawable.tile5)
                    this.isRevealed = true
                }
                6 -> {
                    this.setBackgroundResource(R.drawable.tile6)
                    this.isRevealed = true
                }
                7 -> {
                    this.setBackgroundResource(R.drawable.tile7)
                    this.isRevealed = true
                }
                8 -> {
                    this.setBackgroundResource(R.drawable.tile8)
                    this.isRevealed = true
                }
                else -> {
                    this.setBackgroundResource(R.drawable.tile_pressed)
                    this.isRevealed = true
                }
            }
        }
    }

    //utils
    fun makeCustomGestureDetector(){
        val gestureDetector = GestureDetector(context, SingleTapConfirm())
        this.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (!isRevealed) {
                    //on click logic
                    if (gestureDetector.onTouchEvent(event)) {
                        parent!!.smiley.setBackgroundResource(R.drawable.smiley)
                        if(!hasFlag) {
                            if (!hasBomb)
                                parent!!.triggerTile(coords)
                            else {
                                triggerTile()
                                parent!!.revealAll()
                            }
                        }
                        return false
                    //hovered logic
                    } else {
                        if (event.action == MotionEvent.ACTION_DOWN && !hasFlag) {
                            setBackgroundResource(R.drawable.tile_pressed)
                            parent!!.smiley.setBackgroundResource(R.drawable.smiley_o)
                        }
                        else
                            if (event.action == MotionEvent.ACTION_UP && !isRevealed && !hasFlag){
                                setBackgroundResource(R.drawable.tile_unpressed)
                                parent!!.smiley.setBackgroundResource(R.drawable.smiley)
                            }
                    }
                }
                //daca apesi pe un tile deja revealed
                else{
                    if (gestureDetector.onTouchEvent(event)) {
                        //go through all neighbor tiles and count the number of flags
                        var flags = 0
                        for(i in -1..1){
                            for(j in -1..1){
                                //check if the new coordinates are in the board and it's not the tile that is calling
                                if(i == 0 && j == 0 || ((i + coords.first < 0 || i + coords.first >= parent!!.height) || (j + coords.second < 0 || j + coords.second >= parent!!.width)))
                                    continue
                                val neighbor = parent!!.board[coords.first + i][coords.second + j]
                                if(neighbor != null && neighbor.hasFlag)
                                    flags++

                                if(flags == nearbyBombs)
                                    parent!!.revealedAOE(coords)
                            }
                        }


                    }
                    return false
                }
                return false
            }
        })

        this.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(v: View): Boolean {
                parent!!.smiley.setBackgroundResource(R.drawable.smiley)

                if(!isRevealed) {
                    if (!hasFlag) {
                        setBackgroundResource(R.drawable.tile_flag)
                        hasFlag = true
                        parent!!.flagCount += 1
                        parent!!.bombCountText.text = "Bombs left: " + (parent!!.bombCount - parent!!.flagCount)
                        if(hasBomb)
                            ++ parent!!.correctlyFoundBombs
                        Log.d("FLAGS", "CORRECT FOUND: " + parent!!.correctlyFoundBombs)
                        if(parent!!.correctlyFoundBombs == parent!!.bombCount && parent!!.flagCount == parent!!.bombCount) {
                            parent!!.gameState = GameState.WON
                            parent!!.smiley.setBackgroundResource(R.drawable.smiley_won)
                            parent!!.revealUnflagged()
                        }

                    } else {
                        setBackgroundResource(R.drawable.tile_unpressed)
                        hasFlag = false
                        parent!!.flagCount -= 1
                        parent!!.bombCountText.text = "Bombs left: " + (parent!!.bombCount - parent!!.flagCount)
                        if(hasBomb)
                            -- parent!!.correctlyFoundBombs
                    }
                }
                return false
            }
        })


    }
    private class SingleTapConfirm : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent): Boolean {
            return true
        }

    }
}