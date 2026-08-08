package Player

import godot.annotation.Export
import godot.annotation.Script
import godot.annotation.Register
import godot.annotation.Visible
import godot.api.Area3D
import godot.api.AudioStreamPlayer3D
import godot.api.Curve
import godot.api.Node
import godot.api.Node3D
import godot.core.Vector3
import godot.extension.connectMethod
import godot.global.GD
import shared.Damageable

@Script
class Bullet : Node3D() {

    @Export
    lateinit var scaleDecay: Curve

    @Export
    var distanceLimit: Float = 5f

    @Visible
    var shooter: Node? = null

    @Visible
    var velocity: Vector3 = Vector3.ZERO

    @Export
    lateinit var area: Area3D

    @Export
    lateinit var bulletVisuals: Node3D

    @Export
    lateinit var projectileSound: AudioStreamPlayer3D

    private var timeAlive = 0.0
    private var aliveLimit = 0.0

    override fun _ready() {
        area.bodyEntered.connectMethod(this, Bullet::onBodyEntered)
        lookAt(globalPosition + velocity)
        aliveLimit = distanceLimit / velocity.length()
        projectileSound.pitchScale = GD.randfn(1f, 0.1f)
        projectileSound.play()
    }

    override fun _process(delta: Double) {
        globalPosition += velocity * delta
        timeAlive += delta

        bulletVisuals.scale = Vector3.ONE * scaleDecay.sample((timeAlive / aliveLimit).toFloat())

        if (timeAlive > aliveLimit) {
            queueFree()
        }
    }

    @Register
    fun onBodyEntered(body: Node3D) {
        if (body == shooter) return

        val impactPoint = globalPosition - body.globalPosition

        if (body is Damageable) {
            body.damage(impactPoint, velocity)
        }

//        if (body.isInGroup("damageables".asStringName())) {
//            if (body.hasMethod("damage".asStringName())) {
//                body.call("damage".asStringName(), impactPoint, velocity)
//            }
//        }
    }
}
