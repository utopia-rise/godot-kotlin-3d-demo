package Player

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.api.Area3D
import godot.api.AudioStreamPlayer3D
import godot.api.Curve
import godot.api.Node
import godot.api.Node3D
import godot.core.Vector3
import godot.global.GD
import shared.Damageable

@RegisterClass
class Bullet : Node3D() {

    @Export
    @RegisterProperty
    lateinit var scaleDecay: Curve

    @Export
    @RegisterProperty
    var distanceLimit: Float = 5f

    @RegisterProperty
    var shooter: Node? = null

    @RegisterProperty
    var velocity: Vector3 = Vector3.ZERO

    @Export
    @RegisterProperty
    lateinit var area: Area3D

    @Export
    @RegisterProperty
    lateinit var bulletVisuals: Node3D

    @Export
    @RegisterProperty
    lateinit var projectileSound: AudioStreamPlayer3D

    private var timeAlive = 0.0
    private var aliveLimit = 0.0

    @RegisterFunction
    override fun _ready() {
        area.bodyEntered.connect(this, Bullet::onBodyEntered)
        lookAt(globalPosition + velocity)
        aliveLimit = distanceLimit / velocity.length()
        projectileSound.pitchScale = GD.randfn(1f, 0.1f)
        projectileSound.play()
    }

    @RegisterFunction
    override fun _process(delta: Double) {
        globalPosition += velocity * delta
        timeAlive += delta

        bulletVisuals.scale = Vector3.ONE * scaleDecay.sample((timeAlive / aliveLimit).toFloat())

        if (timeAlive > aliveLimit) {
            queueFree()
        }
    }

    @RegisterFunction
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