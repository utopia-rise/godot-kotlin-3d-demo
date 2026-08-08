package Player.GrenadeVisuals.grenade

import godot.annotation.Export
import godot.annotation.Script
import godot.api.AnimationPlayer
import godot.api.Node3D
import godot.core.Vector3
import godot.core.asStringName

@Script
class GrenadeVisuals : Node3D() {
    @Export
    lateinit var animationPlayer: AnimationPlayer

    private val rotationAxis = Vector3.RIGHT.normalized()

    override fun _ready() {
        animationPlayer.play("wave".asStringName())
    }

    override fun _process(delta: Double) {
        rotateObjectLocal(rotationAxis, (10 * delta).toFloat())
    }
}
