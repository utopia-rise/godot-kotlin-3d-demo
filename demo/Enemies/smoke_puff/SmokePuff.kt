package Enemies.smoke_puff

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.annotation.RegisterSignal
import godot.api.AnimationPlayer
import godot.api.AudioStreamPlayer3D
import godot.api.Node
import godot.api.Node3D
import godot.core.Signal0
import godot.core.signal0
import godot.extension.connectLambda

@RegisterClass
class SmokePuff : Node3D() {

    @RegisterSignal
    val full: Signal0 by signal0()

    @Export
    @RegisterProperty
    lateinit var smokeSoundsRoot: Node

    @Export
    @RegisterProperty
    lateinit var player: AnimationPlayer

    @RegisterFunction
    override fun _ready() {
        (smokeSoundsRoot.getChildren().random() as AudioStreamPlayer3D).play()

        player.play("poof")
        player.animationFinished.connectLambda { queueFree() }
    }

    @RegisterFunction
    fun smokeAtFullDensity() {
        full.emit()
    }
}
