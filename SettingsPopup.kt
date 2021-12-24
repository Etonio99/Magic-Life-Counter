package com.example.magiclifecounter

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.settings_popup.*

class SettingsPopup : AppCompatActivity() {

    object SettingsData {
        var rotatePlayers = true
        var useTimers = false
        var timerLength: Long = 60000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_popup)

        btnBack.setOnClickListener {
            super.onBackPressed()
        }

        btnReset.setOnClickListener {
            resetAndReturn()
        }

        //region Player Count
        btn1Player.setOnClickListener {
            MainActivity.GameData.totalPlayers = 1
            resetAndReturn()
        }

        btn2Player.setOnClickListener {
            MainActivity.GameData.totalPlayers = 2
            resetAndReturn()
        }

        btn3Player.setOnClickListener {
            MainActivity.GameData.totalPlayers = 3
            resetAndReturn()
        }

        btn4Player.setOnClickListener {
            MainActivity.GameData.totalPlayers = 4
            resetAndReturn()
        }

        btn5Player.setOnClickListener {
            MainActivity.GameData.totalPlayers = 5
            resetAndReturn()
        }

        btn6Player.setOnClickListener {
            MainActivity.GameData.totalPlayers = 6
            resetAndReturn()
        }
        //endregion

        //region Starting Health
        btn20StartHealth.setOnClickListener {
            MainActivity.GameData.startingHP = 20
            resetAndReturn()
        }

        btn30StartHealth.setOnClickListener {
            MainActivity.GameData.startingHP = 30
            resetAndReturn()
        }

        btn40StartHealth.setOnClickListener {
            MainActivity.GameData.startingHP = 40
            resetAndReturn()
        }

        btn50StartHealth.setOnClickListener {
            MainActivity.GameData.startingHP = 50
            resetAndReturn()
        }
        //endregion

        switchRotatePlayers.isChecked = SettingsData.rotatePlayers
        switchRotatePlayers.setOnCheckedChangeListener { _, isChecked ->
            SettingsData.rotatePlayers = isChecked
        }

        switchUseTimers.isChecked = SettingsData.useTimers
        switchUseTimers.setOnCheckedChangeListener { _, isChecked ->
            SettingsData.useTimers = isChecked
        }

        btnTimerSettings.setOnClickListener {
            startNewActivity(Intent(this, TimerSettings::class.java))
        }

    }

    private fun resetBoard() {
        val startHP = MainActivity.GameData.startingHP
        for (i in 0 until MainActivity.GameData.playerHPs.count()) {
            MainActivity.GameData.playerHPs[i] = startHP
            MainActivity.GameData.allCommanderDamage = listOf(mutableListOf(0, 0, 0, 0, 0, 0), mutableListOf(0, 0, 0, 0, 0, 0), mutableListOf(0, 0, 0, 0, 0 ,0), mutableListOf(0, 0, 0, 0, 0 ,0), mutableListOf(0, 0, 0, 0, 0 ,0), mutableListOf(0, 0, 0, 0, 0 ,0))
        }
    }

    private fun resetAndReturn() {
        resetBoard()
        super.onBackPressed()
    }

    private fun startNewActivity(intent: Intent) {
        startActivity(intent)
    }

}
