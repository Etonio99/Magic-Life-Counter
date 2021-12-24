package com.example.magiclifecounter

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.colors_menu.*

class ColorsMenu : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.colors_menu)

        btnBack.setOnClickListener {
            super.onBackPressed()
        }

        btnPlayerColor1.setOnClickListener {
            MainActivity.GameData.selectedColor = ContextCompat.getColor(applicationContext, R.color.player_1)
            goBack()
        }

        btnPlayerColor2.setOnClickListener {
            MainActivity.GameData.selectedColor = ContextCompat.getColor(applicationContext, R.color.player_2)
            goBack()
        }

        btnPlayerColor3.setOnClickListener {
            MainActivity.GameData.selectedColor = ContextCompat.getColor(applicationContext, R.color.player_3)
            goBack()
        }

        btnPlayerColor4.setOnClickListener {
            MainActivity.GameData.selectedColor = ContextCompat.getColor(applicationContext, R.color.player_4)
            goBack()
        }

        btnColor1.setOnClickListener {
            MainActivity.GameData.selectedColor = ContextCompat.getColor(applicationContext, R.color.cardColor1)
            goBack()
        }

        btnColor2.setOnClickListener {
            MainActivity.GameData.selectedColor = ContextCompat.getColor(applicationContext, R.color.cardColor2)
            goBack()
        }

        btnColor3.setOnClickListener {
            MainActivity.GameData.selectedColor = ContextCompat.getColor(applicationContext, R.color.cardColor3)
            goBack()
        }

        btnColor4.setOnClickListener {
            MainActivity.GameData.selectedColor = ContextCompat.getColor(applicationContext, R.color.cardColor4)
            goBack()
        }

        btnColor5.setOnClickListener {
            MainActivity.GameData.selectedColor = ContextCompat.getColor(applicationContext, R.color.cardColor5)
            goBack()
        }

        btnColor6.setOnClickListener {
            MainActivity.GameData.selectedColor = ContextCompat.getColor(applicationContext, R.color.cardColor6)
            goBack()
        }

        btnColor7.setOnClickListener {
            MainActivity.GameData.selectedColor = ContextCompat.getColor(applicationContext, R.color.cardColor7)
            goBack()
        }

        btnColor8.setOnClickListener {
            MainActivity.GameData.selectedColor = ContextCompat.getColor(applicationContext, R.color.cardColor8)
            goBack()
        }

        btnColor9.setOnClickListener {
            MainActivity.GameData.selectedColor = ContextCompat.getColor(applicationContext, R.color.cardColor9)
            goBack()
        }

        btnColor10.setOnClickListener {
            MainActivity.GameData.selectedColor = ContextCompat.getColor(applicationContext, R.color.cardColor10)
            goBack()
        }

        btnColor11.setOnClickListener {
            MainActivity.GameData.selectedColor = ContextCompat.getColor(applicationContext, R.color.cardColor11)
            goBack()
        }

        btnColor12.setOnClickListener {
            MainActivity.GameData.selectedColor = ContextCompat.getColor(applicationContext, R.color.cardColor12)
            goBack()
        }
    }

    private fun goBack() {
        MainActivity.GameData.changeColor = true
        super.onBackPressed()
    }

}