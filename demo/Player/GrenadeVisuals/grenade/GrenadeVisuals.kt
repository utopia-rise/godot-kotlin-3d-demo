package Player.GrenadeVisuals.grenade

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.api.AnimationPlayer
import godot.api.Node3D
import godot.core.Vector3
import godot.core.asStringName

@RegisterClass
class GrenadeVisuals : Node3D() {
    @Export
    @RegisterProperty
    lateinit var animationPlayer: AnimationPlayer

    private val rotationAxis = Vector3.RIGHT.normalized()

    @RegisterFunction
    override fun _ready() {
        animationPlayer.play("wave".asStringName())
    }

    @RegisterFunction
    override fun _process(delta: Double) {
        rotateObjectLocal(rotationAxis, (10 * delta).toFloat())
    }
}