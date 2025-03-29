package Player

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.api.Area3D
import godot.api.CollisionShape3D
import godot.api.Node3D
import godot.core.asStringName
import shared.Damageable

@RegisterClass
class MeleeAttackArea : Area3D() {
    @Export
    @RegisterProperty
    lateinit var collisionShape: CollisionShape3D

    @RegisterFunction
    override fun _ready() {
        bodyEntered.connect(this, MeleeAttackArea::onBodyEntered)
    }

    @RegisterFunction
    fun activate() {
        collisionShape.setDeferred(collisionShape::disabled.name.asStringName(), false)
    }

    @RegisterFunction
    fun deactivate() {
        collisionShape.setDeferred(collisionShape::disabled.name.asStringName(), true)
    }

    @RegisterFunction
    fun onBodyEntered(body: Node3D) {
        if (body is Damageable) {
            val impactPoint = globalPosition - body.globalPosition
            val force = -impactPoint

            body.damage(impactPoint, force)
        }
    }
}