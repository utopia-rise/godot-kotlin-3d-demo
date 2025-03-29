package Enemies

import Enemies.beetleBot.BeetleBotSkin
import Player.Player
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.api.AnimationPlayer
import godot.api.Area3D
import godot.api.AudioStreamPlayer3D
import godot.api.CollisionShape3D
import godot.api.NavigationAgent3D
import godot.api.Node
import godot.api.Node3D
import godot.core.Vector3
import godot.core.asStringName

@RegisterClass
class Beetle : Enemy() {

    @Export
    @RegisterProperty
    override var coinsCount = 5

    @Export
    @RegisterProperty
    var shootTimer = 1.5

    @Export
    @RegisterProperty
    var bulletSpeed = 6.0

    @Export
    @RegisterProperty
    lateinit var reactionAnimationPlayer: AnimationPlayer

    @Export
    @RegisterProperty
    lateinit var detectionArea: Area3D

    @Export
    @RegisterProperty
    lateinit var beetleSkin: BeetleBotSkin

    @Export
    @RegisterProperty
    lateinit var navigationAgent: NavigationAgent3D

    @Export
    @RegisterProperty
    lateinit var deathCollisionShape: CollisionShape3D

    @Export
    @RegisterProperty
    lateinit var defeatSound: AudioStreamPlayer3D

    private val foundPlayerName = "found_player".asStringName()
    private val lostPlayerName = "lost_player".asStringName()
    private val disabledName = "disabled".asStringName()

    var target: Node3D? = null
    var alive = true

    @RegisterFunction
    override fun _ready() {
        detectionArea.bodyEntered.connect(this, Beetle::onBodyEntered)
        detectionArea.bodyExited.connect(this, Beetle::onBodyExited)
        beetleSkin.idle()
    }

    @RegisterFunction
    override fun _physicsProcess(delta: Double) {
        if (!alive) return
        target?.let {
            beetleSkin.walk()
            val targetLookPosition = it.globalPosition
            targetLookPosition.y = globalPosition.y
            if (targetLookPosition != Vector3.ZERO) {
                lookAt(targetLookPosition)
            }

            navigationAgent.targetPosition = it.globalPosition

            val nextLocation = navigationAgent.getNextPathPosition()

            if (!navigationAgent.isTargetReached()) {
                var direction = (nextLocation - globalPosition)
                direction.y = 0.0
                direction = direction.normalized()

                val collision = moveAndCollide(direction * delta * 3)
                if (collision != null) {
                    val collider = collision.getCollider()
                    if (collider is Player) {
                        val impactPoint: Vector3 = globalPosition - collider.globalPosition
                        var force = -impactPoint
                        // Throws player up a little bit
                        force.y = 0.5
                        force *= 10.0
                        collider.damage(impactPoint, force)
                        beetleSkin.attack()
                    }
                }
            }
        }
    }

    @RegisterFunction
    override fun damage(impactPoint: Vector3, force: Vector3) {
        lockRotation = false
        applyImpulse(force.limitLength(3.0), impactPoint)

        if (!alive) {
            return
        }

        defeatSound.play()
        alive = false
        beetleSkin.powerOff()

        detectionArea.bodyEntered.disconnect(this, Beetle::onBodyEntered)
        detectionArea.bodyExited.disconnect(this, Beetle::onBodyExited)
        target = null
        deathCollisionShape.setDeferred(disabledName, false)

        axisLockAngularX = false
        axisLockAngularY = false
        axisLockAngularZ = false
        gravityScale = 1.0f

        death()
    }

    @RegisterFunction
    fun onBodyEntered(body: Node) {
        if (body is Player) {
            target = body
            reactionAnimationPlayer.play(foundPlayerName)
        }
    }

    @RegisterFunction
    fun onBodyExited(body: Node) {
        if (body is Player) {
            target = null
            reactionAnimationPlayer.play(lostPlayerName)
            beetleSkin.idle()
        }
    }
}
