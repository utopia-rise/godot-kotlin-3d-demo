package Player

import godot.annotation.Export
import godot.annotation.Script
import godot.annotation.Register
import godot.api.Area3D
import godot.api.CollisionShape3D
import godot.api.Node3D
import godot.core.asStringName
import godot.extension.connectMethod
import shared.Damageable

@Script
class MeleeAttackArea : Area3D() {
    @Export
    lateinit var collisionShape: CollisionShape3D

    override fun _ready() {
        bodyEntered.connectMethod(this, MeleeAttackArea::onBodyEntered)
    }

    @Register
    fun activate() {
        collisionShape.setDeferred(collisionShape::disabled.name.asStringName(), false)
    }

    @Register
    fun deactivate() {
        collisionShape.setDeferred(collisionShape::disabled.name.asStringName(), true)
    }

    @Register
    fun onBodyEntered(body: Node3D) {
        if (body is Damageable) {
            val impactPoint = globalPosition - body.globalPosition
            val force = -impactPoint

            body.damage(impactPoint, force)
        }
    }
}
