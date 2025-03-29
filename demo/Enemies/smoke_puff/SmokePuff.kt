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
import godot.core.Callable
import godot.core.Signal0
import godot.core.asStringName
import godot.core.signal0

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

    private val poofName = "poof".asStringName()
    private val queueFreeName = "queue_free".asStringName()

    @RegisterFunction
    override fun _ready() {
        (smokeSoundsRoot.getChildren().random() as AudioStreamPlayer3D).play()

        player.play(poofName)
        val callable = Callable(this, queueFreeName).unbind(1)
        player.animationFinished.connect(callable)
    }

    @RegisterFunction
    fun smokeAtFullDensity() {
        full.emit()
    }
}