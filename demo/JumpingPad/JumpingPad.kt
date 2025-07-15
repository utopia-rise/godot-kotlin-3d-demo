package JumpingPad

import Player.Player
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.api.Area3D
import godot.api.Node
import godot.api.Node3D
import godot.api.Tween
import godot.core.Vector3
import godot.core.asNodePath

@RegisterClass
class JumpingPad : Area3D() {

    @Export
    @RegisterProperty
    var impulseStrength = 10.0

    @Export
    @RegisterProperty
    lateinit var mushroom: Node3D

    @RegisterFunction
    override fun _ready() {
        bodyEntered.connect(this, JumpingPad::onBodyEntered)
    }

    @RegisterFunction
    fun onBodyEntered(body: Node) {
        if (body is Player) {
            body.velocity = (Vector3.UP * body.jumpInitialImpulse) + (transform.basis * Vector3.UP * impulseStrength)

            val tween = createTween()!!
            mushroom.scale.y = 0.4
            tween.tweenProperty(mushroom, "scale:y".asNodePath(), 1.0, 1.0)
                ?.setEase(Tween.EaseType.OUT)
                ?.setTrans(Tween.TransitionType.ELASTIC)
        }
    }
}
