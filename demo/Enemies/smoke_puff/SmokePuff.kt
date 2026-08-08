package Enemies.smoke_puff

import godot.annotation.Export
import godot.annotation.Script
import godot.annotation.Register
import godot.annotation.Emit
import godot.api.AnimationPlayer
import godot.api.AudioStreamPlayer3D
import godot.api.Node
import godot.api.Node3D
import godot.core.Signal0
import godot.core.signal0
import godot.extension.connectLambda

@Script
class SmokePuff : Node3D() {

    @Emit
    val full: Signal0 by signal0()

    @Export
    lateinit var smokeSoundsRoot: Node

    @Export
    lateinit var player: AnimationPlayer

    override fun _ready() {
        (smokeSoundsRoot.getChildren().random() as AudioStreamPlayer3D).play()

        player.play("poof")
        player.animationFinished.connectLambda { queueFree() }
    }

    @Register
    fun smokeAtFullDensity() {
        full.emit()
    }
}
