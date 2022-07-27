package base.base2.minesweeperandroid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import base.example.minesweeperandroid.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //formal stuff
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get base
        val base = findViewById<ConstraintLayout>(R.id.Base)

        val gameBoard = GameBoard(this, base, 12, 12, 25)



    }

}

