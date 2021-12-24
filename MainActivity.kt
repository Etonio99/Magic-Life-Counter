package com.example.magiclifecounter

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var savedTotalPlayers = 0
    private var activePlayer = 0
    private var lastActivePlayer = 0

    object GameData {
        var totalPlayers = 2

        var startingHP = 20

//        var playerHPs = mutableListOf(startingHP, startingHP, startingHP, startingHP, startingHP, startingHP)
        var playerHPs = MutableList(6) { startingHP }
        var allCommanderDamage = listOf(mutableListOf(0, 0, 0, 0, 0, 0), mutableListOf(0, 0, 0, 0, 0, 0), mutableListOf(0, 0, 0, 0, 0 ,0), mutableListOf(0, 0, 0, 0, 0 ,0), mutableListOf(0, 0, 0, 0, 0 ,0), mutableListOf(0, 0, 0, 0, 0 ,0))

        //Colors
        var selectedCard = 0
        var changeColor = false
        var selectedColor = 0
    }

    //Switched to true when the change colors button is toggled on.
    var changeColors = false

    var dealingCommanderDamage = false
    var attackingCommander = 0

    var cancelTap = false
    var positive = true

    var dragHealth = 0
    var touchY = 0f
    private val dragSensitivity = 60

    private val handler = Handler(Looper.getMainLooper())
    var boundaries: Rect? = null

    private var longPressCheckerRunning = false

    var longPressChecker = Runnable {
        longPressCheckerRunning = true
        var amount = 5
        if (!positive)
            amount = -5
        if (dealingCommanderDamage)
            changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, amount)
        else
            changeHealth(GameData.selectedCard - 1, amount)
        updateCardUI()
        cancelTap = true
        if (longPressCheckerRunning)
            startLongPressChecker()
    }

    private fun startLongPressChecker() {
        handler.postDelayed(longPressChecker, 350)
    }

    private fun stopLongPressChecker() {
        longPressCheckerRunning = false
        handler.removeCallbacks(longPressChecker)
    }

    private lateinit var playerHealthTexts: List<TextView>

    private lateinit var startCircles: List<ImageView>
    private lateinit var startCircleTexts: List<TextView>

    private lateinit var timers: List<TextView>

    var allTimeLeftInMillis = mutableListOf<Long>(SettingsPopup.SettingsData.timerLength, SettingsPopup.SettingsData.timerLength, SettingsPopup.SettingsData.timerLength, SettingsPopup.SettingsData.timerLength, SettingsPopup.SettingsData.timerLength, SettingsPopup.SettingsData.timerLength)
    //region Timers
    private val timerObject = object: CountDownTimer(SettingsPopup.SettingsData.timerLength, 250) {
        override fun onTick(millisUntilFinished: Long) {
            allTimeLeftInMillis[activePlayer] = millisUntilFinished
            updateTimerText(activePlayer)
        }

        override fun onFinish() {
            TODO("Not yet implemented")
        }
    }
    //endregion
    private val allTimers = mutableListOf<CountDownTimer>(timerObject, timerObject, timerObject, timerObject, timerObject, timerObject)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerHealthTexts = listOf(p1HPText, p2HPText, p3HPText, p4HPText, p5HPText, p6HPText)

        startCircles = listOf(startCircle1, startCircle2, startCircle3, startCircle4, startCircle5, startCircle6)
        startCircleTexts = listOf(startCircleText1, startCircleText2, startCircleText3, startCircleText4, startCircleText5, startCircleText6)

        timers = listOf(p1TimerText, p2TimerText, p3TimerText, p4TimerText, p5TimerText, p6TimerText)

        updateAllUI()

        //region Player Cards
        //region Player 1
        //Player 1 Card
        p1Card.setOnTouchListener { v, event ->
            v.performClick()

            if (dealingCommanderDamage && attackingCommander == 0)
                return@setOnTouchListener true

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    GameData.selectedCard = 1

                    if (changeColors) {
                        cancelTap = true
                        return@setOnTouchListener true
                    }

                    boundaries = Rect(v.left, v.top, v.right, v.bottom)
                    startLongPressChecker()

                    //Get tap y position relative to button
                    touchY = event.y

                    if (touchY < p1Card.height / 2f) {
                        //Tapped in top half
                        positive = true
                        p1Tap1.visibility = View.VISIBLE
                    }
                    else {
                        //Tapped in bottom half
                        positive = false
                        p1Tap2.visibility = View.VISIBLE
                    }

                    updateCardUI()
                }

                MotionEvent.ACTION_UP -> {
                    stopLongPressChecker()

                    makeTapsInvisible()

                    if (changeColors) {
                        gotoColors()
                        changeChangeColors(false)
                    }

                    if (cancelTap) {
                        cancelTap = false
                        return@setOnTouchListener true
                    }

                    //Check that the player did not drag to add health...
                    if(dragHealth == 0) {
                        if (touchY < p1Card.height / 2f) {
                            //Tapped in top half
                            if (dealingCommanderDamage)
                                changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, 1)
                            else
                                changeHealth(GameData.selectedCard - 1,1)
                        }
                        else {
                            //Tapped in bottom half
                            if (dealingCommanderDamage)
                                changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, -1)
                            else
                                changeHealth(GameData.selectedCard - 1,-1)
                        }
                    }
                    //But if they did, add the drag health
                    else {
                        if (dealingCommanderDamage)
                            changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, dragHealth)
                        else
                            changeHealth(GameData.selectedCard - 1,dragHealth)
                        dragHealth = 0
                    }

                    updateCardUI()
                }

                MotionEvent.ACTION_MOVE -> {
                    if (abs(dragHealth) > 0) {
                        stopLongPressChecker()

                        makeTapsInvisible()
                    }

                    if (longPressCheckerRunning)
                        return@setOnTouchListener true

                    val newY = event.y

                    dragHealth = ((touchY - newY) / dragSensitivity).toInt()
                    if (dealingCommanderDamage)
                        p1HPText.text = (GameData.allCommanderDamage[attackingCommander][0] + dragHealth).toString()
                    else
                        p1HPText.text = (GameData.playerHPs[0] + dragHealth).toString()
                }

            }

            true
        }

        btnP1Commander.setOnClickListener {
            changeChangeColors(false)

            if (dealingCommanderDamage)
                return@setOnClickListener

            dealingCommanderDamage = true
            attackingCommander = 0
            p1CommanderCard.visibility = View.VISIBLE
            updateCardUI()
        }

        btnP1CommanderDone.setOnClickListener {
            dealingCommanderDamage = false
            p1CommanderCard.visibility = View.INVISIBLE
            updateCardUI()
        }
        //endregion

        //region Player 2
        //Player 2 Card
        p2Card.setOnTouchListener { v, event ->
            v.performClick()

            if (dealingCommanderDamage && attackingCommander == 1)
                return@setOnTouchListener true

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    GameData.selectedCard = 2

                    if (changeColors) {
                        cancelTap = true
                        return@setOnTouchListener true
                    }

                    boundaries = Rect(v.left, v.top, v.right, v.bottom)
                    startLongPressChecker()

                    //Get tap y position relative to button
                    touchY = event.y

                    if (touchY < p2Card.height / 2f) {
                        //Tapped in top half
                        positive = true
                        p2Tap1.visibility = View.VISIBLE
                    }
                    else {
                        //Tapped in bottom half
                        positive = false
                        p2Tap2.visibility = View.VISIBLE
                    }

                    updateCardUI()
                }

                MotionEvent.ACTION_UP -> {
                    stopLongPressChecker()

                    makeTapsInvisible()

                    if (changeColors) {
                        gotoColors()
                        changeChangeColors(false)
                    }

                    if (cancelTap) {
                        cancelTap = false
                        return@setOnTouchListener true
                    }

                    //Check that the player did not drag to add health...
                    if(dragHealth == 0) {
                        if (touchY < p2Card.height / 2f) {
                            //Tapped in top half
                            if (dealingCommanderDamage)
                                changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, 1)
                            else
                                changeHealth(GameData.selectedCard - 1,1)
                        }
                        else {
                            //Tapped in bottom half
                            if (dealingCommanderDamage)
                                changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, -1)
                            else
                                changeHealth(GameData.selectedCard - 1,-1)
                        }
                    }
                    //But if they did, add the drag health
                    else {
                        if (dealingCommanderDamage)
                            changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, dragHealth)
                        else
                            changeHealth(GameData.selectedCard - 1,dragHealth)
                        dragHealth = 0
                    }

                    updateCardUI()
                }

                MotionEvent.ACTION_MOVE -> {
                    if (abs(dragHealth) > 0) {
                        stopLongPressChecker()

                        makeTapsInvisible()
                    }

                    if (longPressCheckerRunning)
                        return@setOnTouchListener true

                    val newY = event.y

                    dragHealth = ((touchY - newY) / dragSensitivity).toInt()
                    if (dealingCommanderDamage)
                        p2HPText.text = (GameData.allCommanderDamage[attackingCommander][1] + dragHealth).toString()
                    else
                        p2HPText.text = (GameData.playerHPs[1] + dragHealth).toString()
                }

            }

            true
        }

        btnP2Commander.setOnClickListener {
            changeChangeColors(false)

            if (dealingCommanderDamage)
                return@setOnClickListener

            dealingCommanderDamage = true
            attackingCommander = 1
            p2CommanderCard.visibility = View.VISIBLE
            updateCardUI()
        }

        btnP2CommanderDone.setOnClickListener {
            dealingCommanderDamage = false
            p2CommanderCard.visibility = View.INVISIBLE
            updateCardUI()
        }
        //endregion

        //region Player 3
        //Player 3 Card
        p3Card.setOnTouchListener { v, event ->
            v.performClick()

            if (dealingCommanderDamage && attackingCommander == 2)
                return@setOnTouchListener true

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    GameData.selectedCard = 3

                    if (changeColors) {
                        cancelTap = true
                        return@setOnTouchListener true
                    }

                    boundaries = Rect(v.left, v.top, v.right, v.bottom)
                    startLongPressChecker()

                    //Get tap y position relative to button
                    touchY = event.y

                    if (touchY < p3Card.height / 2f) {
                        //Tapped in top half
                        positive = true
                        p3Tap1.visibility = View.VISIBLE
                    }
                    else {
                        //Tapped in bottom half
                        positive = false
                        p3Tap2.visibility = View.VISIBLE
                    }

                    updateCardUI()
                }

                MotionEvent.ACTION_UP -> {
                    stopLongPressChecker()

                    makeTapsInvisible()

                    if (changeColors) {
                        gotoColors()
                        changeChangeColors(false)
                    }

                    if (cancelTap) {
                        cancelTap = false
                        return@setOnTouchListener true
                    }

                    //Check that the player did not drag to add health...
                    if(dragHealth == 0) {
                        if (touchY < p1Card.height / 2f) {
                            //Tapped in top half
                            if (dealingCommanderDamage)
                                changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, 1)
                            else
                                changeHealth(GameData.selectedCard - 1,1)
                        }
                        else {
                            //Tapped in bottom half
                            if (dealingCommanderDamage)
                                changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, -1)
                            else
                                changeHealth(GameData.selectedCard - 1,-1)
                        }
                    }
                    //But if they did, add the drag health
                    else {
                        if (dealingCommanderDamage)
                            changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, dragHealth)
                        else
                            changeHealth(GameData.selectedCard - 1,dragHealth)
                        dragHealth = 0
                    }

                    updateCardUI()
                }

                MotionEvent.ACTION_MOVE -> {
                    if (abs(dragHealth) > 0) {
                        stopLongPressChecker()

                        makeTapsInvisible()
                    }

                    if (longPressCheckerRunning)
                        return@setOnTouchListener true

                    val newY = event.y

                    dragHealth = ((touchY - newY) / dragSensitivity).toInt()
                    if (dealingCommanderDamage)
                        p3HPText.text = (GameData.allCommanderDamage[attackingCommander][2] + dragHealth).toString()
                    else
                        p3HPText.text = (GameData.playerHPs[2] + dragHealth).toString()
                }

            }

            true
        }

        btnP3Commander.setOnClickListener {
            changeChangeColors(false)

            if (dealingCommanderDamage)
                return@setOnClickListener

            dealingCommanderDamage = true
            attackingCommander = 2
            p3CommanderCard.visibility = View.VISIBLE
            updateCardUI()
        }

        btnP3CommanderDone.setOnClickListener {
            dealingCommanderDamage = false
            p3CommanderCard.visibility = View.INVISIBLE
            updateCardUI()
        }
        //endregion

        //region Player 4
        //Player 4 Card
        p4Card.setOnTouchListener { v, event ->
            v.performClick()

            if (dealingCommanderDamage && attackingCommander == 3)
                return@setOnTouchListener true

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    GameData.selectedCard = 4

                    if (changeColors) {
                        cancelTap = true
                        return@setOnTouchListener true
                    }

                    boundaries = Rect(v.left, v.top, v.right, v.bottom)
                    startLongPressChecker()

                    //Get tap y position relative to button
                    touchY = event.y

                    if (touchY < p4Card.height / 2f) {
                        //Tapped in top half
                        positive = true
                        p4Tap1.visibility = View.VISIBLE
                    }
                    else {
                        //Tapped in bottom half
                        positive = false
                        p4Tap2.visibility = View.VISIBLE
                    }

                    updateCardUI()
                }

                MotionEvent.ACTION_UP -> {
                    stopLongPressChecker()

                    makeTapsInvisible()

                    if (changeColors) {
                        gotoColors()
                        changeChangeColors(false)
                    }

                    if (cancelTap) {
                        cancelTap = false
                        return@setOnTouchListener true
                    }

                    //Check that the player did not drag to add health...
                    if(dragHealth == 0) {
                        if (touchY < p4Card.height / 2f) {
                            //Tapped in top half
                            if (dealingCommanderDamage)
                                changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, 1)
                            else
                                changeHealth(GameData.selectedCard - 1,1)
                        }
                        else {
                            //Tapped in bottom half
                            if (dealingCommanderDamage)
                                changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, -1)
                            else
                                changeHealth(GameData.selectedCard - 1,-1)
                        }
                    }
                    //But if they did, add the drag health
                    else {
                        if (dealingCommanderDamage)
                            changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, dragHealth)
                        else
                            changeHealth(GameData.selectedCard - 1,dragHealth)
                        dragHealth = 0
                    }

                    updateCardUI()
                }

                MotionEvent.ACTION_MOVE -> {
                    if (abs(dragHealth) > 0) {
                        stopLongPressChecker()

                        makeTapsInvisible()
                    }

                    if (longPressCheckerRunning)
                        return@setOnTouchListener true

                    val newY = event.y

                    dragHealth = ((touchY - newY) / dragSensitivity).toInt()
                    if (dealingCommanderDamage)
                        p4HPText.text = (GameData.allCommanderDamage[attackingCommander][3] + dragHealth).toString()
                    else
                        p4HPText.text = (GameData.playerHPs[3] + dragHealth).toString()
                }

            }

            true
        }

        btnP4Commander.setOnClickListener {
            changeChangeColors(false)

            if (dealingCommanderDamage)
                return@setOnClickListener

            dealingCommanderDamage = true
            attackingCommander = 3
            p4CommanderCard.visibility = View.VISIBLE
            updateCardUI()
        }

        btnP4CommanderDone.setOnClickListener {
            dealingCommanderDamage = false
            p4CommanderCard.visibility = View.INVISIBLE
            updateCardUI()
        }
        //endregion

        //region Player 5
        //Player 5 Card
        p5Card.setOnTouchListener { v, event ->
            v.performClick()

            if (dealingCommanderDamage && attackingCommander == 4)
                return@setOnTouchListener true

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    GameData.selectedCard = 5

                    if (changeColors) {
                        cancelTap = true
                        return@setOnTouchListener true
                    }

                    boundaries = Rect(v.left, v.top, v.right, v.bottom)
                    startLongPressChecker()

                    //Get tap y position relative to button
                    touchY = event.y

                    if (touchY < p5Card.height / 2f) {
                        //Tapped in top half
                        positive = true
                        p5Tap1.visibility = View.VISIBLE
                    }
                    else {
                        //Tapped in bottom half
                        positive = false
                        p5Tap2.visibility = View.VISIBLE
                    }

                    updateCardUI()
                }

                MotionEvent.ACTION_UP -> {
                    stopLongPressChecker()

                    makeTapsInvisible()

                    if (changeColors) {
                        gotoColors()
                        changeChangeColors(false)
                    }

                    if (cancelTap) {
                        cancelTap = false
                        return@setOnTouchListener true
                    }

                    //Check that the player did not drag to add health...
                    if(dragHealth == 0) {
                        if (touchY < p5Card.height / 2f) {
                            //Tapped in top half
                            if (dealingCommanderDamage)
                                changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, 1)
                            else
                                changeHealth(GameData.selectedCard - 1,1)
                        }
                        else {
                            //Tapped in bottom half
                            if (dealingCommanderDamage)
                                changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, -1)
                            else
                                changeHealth(GameData.selectedCard - 1,-1)
                        }
                    }
                    //But if they did, add the drag health
                    else {
                        if (dealingCommanderDamage)
                            changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, dragHealth)
                        else
                            changeHealth(GameData.selectedCard - 1,dragHealth)
                        dragHealth = 0
                    }

                    updateCardUI()
                }

                MotionEvent.ACTION_MOVE -> {
                    if (abs(dragHealth) > 0) {
                        stopLongPressChecker()

                        makeTapsInvisible()
                    }

                    if (longPressCheckerRunning)
                        return@setOnTouchListener true

                    val newY = event.y

                    dragHealth = ((touchY - newY) / dragSensitivity).toInt()
                    if (dealingCommanderDamage)
                        p5HPText.text = (GameData.allCommanderDamage[attackingCommander][4] + dragHealth).toString()
                    else
                        p5HPText.text = (GameData.playerHPs[4] + dragHealth).toString()
                }

            }

            true
        }

        btnP5Commander.setOnClickListener {
            changeChangeColors(false)

            if (dealingCommanderDamage)
                return@setOnClickListener

            dealingCommanderDamage = true
            attackingCommander = 4
            p5CommanderCard.visibility = View.VISIBLE
            updateCardUI()
        }

        btnP5CommanderDone.setOnClickListener {
            dealingCommanderDamage = false
            p5CommanderCard.visibility = View.INVISIBLE
            updateCardUI()
        }
        //endregion

        //region Player 6
        //Player 6 Card
        p6Card.setOnTouchListener { v, event ->
            v.performClick()

            if (dealingCommanderDamage && attackingCommander == 5)
                return@setOnTouchListener true

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    GameData.selectedCard = 6

                    if (changeColors) {
                        cancelTap = true
                        return@setOnTouchListener true
                    }

                    boundaries = Rect(v.left, v.top, v.right, v.bottom)
                    startLongPressChecker()

                    //Get tap y position relative to button
                    touchY = event.y

                    if (touchY < p6Card.height / 2f) {
                        //Tapped in top half
                        positive = true
                        p6Tap1.visibility = View.VISIBLE
                    }
                    else {
                        //Tapped in bottom half
                        positive = false
                        p6Tap2.visibility = View.VISIBLE
                    }

                    updateCardUI()
                }

                MotionEvent.ACTION_UP -> {
                    stopLongPressChecker()

                    makeTapsInvisible()

                    if (changeColors) {
                        gotoColors()
                        changeChangeColors(false)
                    }

                    if (cancelTap) {
                        cancelTap = false
                        return@setOnTouchListener true
                    }

                    //Check that the player did not drag to add health...
                    if(dragHealth == 0) {
                        if (touchY < p6Card.height / 2f) {
                            //Tapped in top half
                            if (dealingCommanderDamage)
                                changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, 1)
                            else
                                changeHealth(GameData.selectedCard - 1,1)
                        }
                        else {
                            //Tapped in bottom half
                            if (dealingCommanderDamage)
                                changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, -1)
                            else
                                changeHealth(GameData.selectedCard - 1,-1)
                        }
                    }
                    //But if they did, add the drag health
                    else {
                        if (dealingCommanderDamage)
                            changeCommanderDamage(attackingCommander, GameData.selectedCard - 1, dragHealth)
                        else
                            changeHealth(GameData.selectedCard - 1,dragHealth)
                        dragHealth = 0
                    }

                    updateCardUI()
                }

                MotionEvent.ACTION_MOVE -> {
                    if (abs(dragHealth) > 0) {
                        stopLongPressChecker()

                        makeTapsInvisible()
                    }

                    if (longPressCheckerRunning)
                        return@setOnTouchListener true

                    val newY = event.y

                    dragHealth = ((touchY - newY) / dragSensitivity).toInt()
                    if (dealingCommanderDamage)
                        p6HPText.text = (GameData.allCommanderDamage[attackingCommander][5] + dragHealth).toString()
                    else
                        p6HPText.text = (GameData.playerHPs[5] + dragHealth).toString()
                }

            }

            true
        }

        btnP6Commander.setOnClickListener {
            changeChangeColors(false)

            if (dealingCommanderDamage)
                return@setOnClickListener

            dealingCommanderDamage = true
            attackingCommander = 5
            p6CommanderCard.visibility = View.VISIBLE
            updateCardUI()
        }

        btnP6CommanderDone.setOnClickListener {
            dealingCommanderDamage = false
            p6CommanderCard.visibility = View.INVISIBLE
            updateCardUI()
        }
        //endregion
        //endregion

        //region Menu Buttons
        //Colors
        btnColors.setOnClickListener {
            if (dealingCommanderDamage)
                changeChangeColors(false)
            else
                changeChangeColors(!changeColors)
        }

        //Start
        btnStart.setOnClickListener {
            changeChangeColors(false)
            showStartingPlayer()
        }

        //Timer
        btnTimer.setOnClickListener {
            changeChangeColors(false)
            goToNextPlayer()
        }

        btnTimer.setOnLongClickListener {
            changeChangeColors(false)
            startNewActivity(Intent(this, TimerSettings::class.java))

            true
        }

        //Settings
        btnSettings.setOnClickListener {
            changeChangeColors(false)
            startNewActivity(Intent(this, SettingsPopup::class.java))
        }
        //endregion
    }

    private fun changeChangeColors(setting: Boolean) {
        changeColors = setting
        if (changeColors)
            btnColors.setColorFilter(ContextCompat.getColor(applicationContext, R.color.purple_700))
        else
            btnColors.setColorFilter(ContextCompat.getColor(applicationContext, R.color.ui_front_color))
    }

    private fun changeHealth(playerId: Int, amount: Int) {
        GameData.playerHPs[playerId] += amount
    }

    private fun changeCommanderDamage(commanderId: Int, playerId: Int, amount: Int) {
        GameData.allCommanderDamage[commanderId][playerId] += amount
    }

    private fun showStartingPlayer() {
        val rand = Random.nextInt(0, savedTotalPlayers)
        activePlayer = rand
        val selCircle = startCircles[rand]
        val selCircleText = startCircleTexts[rand]

        selCircle.visibility = View.VISIBLE
        selCircleText.visibility = View.VISIBLE

        btnStart.visibility = View.INVISIBLE

        /*if (SettingsPopup.SettingsData.useTimers) {
            startTimer(rand)
            btnTimer.visibility = View.VISIBLE
            for (i in 0 until GameData.totalPlayers)
                updateTimerText(i)
        }
        else
            btnTimer.visibility = View.INVISIBLE*/

        handler.postDelayed({
            selCircle.visibility = View.INVISIBLE
            selCircleText.visibility = View.INVISIBLE
            btnStart.visibility = View.VISIBLE
        }, 2000)
    }

    private fun startTimer(playerId: Int) {
        allTimers[playerId] = object : CountDownTimer(allTimeLeftInMillis[playerId], 250) {
            override fun onTick(millisUntilFinished: Long) {
                allTimeLeftInMillis[activePlayer] = millisUntilFinished
                updateTimerText(activePlayer)
            }

            override fun onFinish() {
                TODO("Not yet implemented")
            }
        }
        allTimers[playerId].start()
    }

    private fun pauseTimer(playerId: Int) {
        allTimers[playerId].cancel()
        //println(allTimeLeftInMillis[0])
    }

    private fun updateTimerText(playerId: Int) {
        val minutes = allTimeLeftInMillis[playerId] / 1000 / 60
        val seconds = allTimeLeftInMillis[playerId] / 1000 % 60
        timers[playerId].text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun goToNextPlayer() {
        if (GameData.totalPlayers <= 3) {
            activePlayer++
            if (activePlayer >= GameData.totalPlayers)
                activePlayer = 0
        }
        else {
            //THIS NEEDS TO BE FIXED TO WORK WITH 6 PLAYERS
            when (lastActivePlayer) {
                0 -> activePlayer = 1
                1 -> activePlayer = 3
                3 -> activePlayer = 2
                2 -> activePlayer = 0
            }
        }

        pauseTimer(lastActivePlayer)
        startTimer(activePlayer)

        updateTimerText(lastActivePlayer)
        updateTimerText(activePlayer)

        lastActivePlayer = activePlayer
    }

    private fun makeTapsInvisible() {
        when (GameData.selectedCard) {
            1 -> {
                p1Tap1.visibility = View.INVISIBLE
                p1Tap2.visibility = View.INVISIBLE
            }
            2 -> {
                p2Tap1.visibility = View.INVISIBLE
                p2Tap2.visibility = View.INVISIBLE
            }
            3 -> {
                p3Tap1.visibility = View.INVISIBLE
                p3Tap2.visibility = View.INVISIBLE
            }
            4 -> {
                p4Tap1.visibility = View.INVISIBLE
                p4Tap2.visibility = View.INVISIBLE
            }
            5 -> {
                p5Tap1.visibility = View.INVISIBLE
                p5Tap2.visibility = View.INVISIBLE
            }
            6 -> {
                p6Tap1.visibility = View.INVISIBLE
                p6Tap2.visibility = View.INVISIBLE
            }
        }
    }

    override fun onResume() {
        cancelTap = false

        updateCardUI()
        updateAllUI()
        updateColors()

        super.onResume()
    }

    private fun updateCardUI() {
        if (dealingCommanderDamage) {
            for (i in 0 until playerHealthTexts.count()) {
                playerHealthTexts[i].text = GameData.allCommanderDamage[attackingCommander][i].toString()
            }
        }
        else {
            for (i in 0 until playerHealthTexts.count()) {
                playerHealthTexts[i].text = GameData.playerHPs[i].toString()
            }
        }
    }

    private fun updateAllUI() {
        //If the amount of players playing changes...
        if (savedTotalPlayers != GameData.totalPlayers) {
            //--- Player Cards ---
            //Visibility
            cardColumn2.visibility = if (GameData.totalPlayers >= 2) View.VISIBLE else View.GONE
            p3Card.visibility = if (GameData.totalPlayers >= 3) View.VISIBLE else View.GONE
            p4Card.visibility = if (GameData.totalPlayers >= 4) View.VISIBLE else View.GONE
            cardColumn3.visibility = if (GameData.totalPlayers >= 5) View.VISIBLE else View.GONE
            p6Card.visibility = if (GameData.totalPlayers >= 6) View.VISIBLE else View.GONE

            //--- Menu Buttons ---
            //btnTimer.visibility = if (GameData.totalPlayers >= 2) View.VISIBLE else View.INVISIBLE

            savedTotalPlayers = GameData.totalPlayers
        }

        //Rotation
        if (SettingsPopup.SettingsData.rotatePlayers) {
            p1Card.rotation = if (GameData.totalPlayers > 2) 180f else 0f
            p2Card.rotation = if (GameData.totalPlayers % 2 == 0 || GameData.totalPlayers >= 5) 180f else 0f
            p5Card.rotation = if (GameData.totalPlayers >= 6) 180f else 0f
        }
        else {
            p1Card.rotation = 0f
            p2Card.rotation = 0f
            p5Card.rotation = 0f
        }

        //Timers
        var visibility = View.VISIBLE
        if (!SettingsPopup.SettingsData.useTimers) visibility = View.GONE
        for (timer in timers) {
            timer.visibility = visibility
        }
    }

    private fun gotoColors() {
        startNewActivity(Intent(this, ColorsMenu::class.java))
    }

    private fun updateColors() {
        if (!GameData.changeColor)
            return
        when (GameData.selectedCard) {
            1 -> p1Card.backgroundTintList = ColorStateList.valueOf(GameData.selectedColor)
            2 -> p2Card.backgroundTintList = ColorStateList.valueOf(GameData.selectedColor)
            3 -> p3Card.backgroundTintList = ColorStateList.valueOf(GameData.selectedColor)
            4 -> p4Card.backgroundTintList = ColorStateList.valueOf(GameData.selectedColor)
            5 -> p5Card.backgroundTintList = ColorStateList.valueOf(GameData.selectedColor)
            6 -> p6Card.backgroundTintList = ColorStateList.valueOf(GameData.selectedColor)
        }
        GameData.changeColor = false
    }

    private fun startNewActivity(intent: Intent) {
        startActivity(intent)
    }
}