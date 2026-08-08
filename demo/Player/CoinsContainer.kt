package Player

import godot.annotation.Export
import godot.annotation.Script
import godot.annotation.Register
import godot.api.HBoxContainer
import godot.api.Label
import godot.api.Timer
import godot.core.asNodePath
import godot.extension.connectMethod

private const val HIDDEN_Y_POS = -100
private const val DISPLAY_Y_POS = 20

@Script
class CoinsContainer : HBoxContainer() {
    @Export
    lateinit var displayTimer: Timer

    @Export
    lateinit var coinsLabel: Label

    override fun _ready() {
        displayTimer.timeout.connectMethod(this, CoinsContainer::onTimeout)
    }

    @Register
    fun updateCoinsAmount(amount: Int) {
        if (displayTimer.isStopped()) {
            createTween().tweenProperty(this, "position:y".asNodePath(), DISPLAY_Y_POS, 0.5)
        }

        displayTimer.start()
        coinsLabel.text = amount.toString()
    }

    @Register
    fun onTimeout() {
        createTween().tweenProperty(this, "position:y".asNodePath(), HIDDEN_Y_POS, 0.5)
    }
}
