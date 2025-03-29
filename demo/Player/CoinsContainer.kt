package Player

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.api.HBoxContainer
import godot.api.Label
import godot.api.Timer
import godot.core.asNodePath

private const val HIDDEN_Y_POS = -100
private const val DISPLAY_Y_POS = 20

@RegisterClass
class CoinsContainer : HBoxContainer() {
    @Export
    @RegisterProperty
    lateinit var displayTimer: Timer

    @Export
    @RegisterProperty
    lateinit var coinsLabel: Label

    @RegisterFunction
    override fun _ready() {
        displayTimer.timeout.connect(this, CoinsContainer::onTimeout)
    }

    @RegisterFunction
    fun updateCoinsAmount(amount: Int) {
        if (displayTimer.isStopped()) {
            createTween()?.tweenProperty(this, "position:y".asNodePath(), DISPLAY_Y_POS, 0.5)
        }

        displayTimer.start()
        coinsLabel.text = amount.toString()
    }

    @RegisterFunction
    fun onTimeout() {
        createTween()?.tweenProperty(this, "position:y".asNodePath(), HIDDEN_Y_POS, 0.5)
    }
}