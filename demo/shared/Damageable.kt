package shared

import godot.annotation.Register
import godot.core.Vector3

interface Damageable {
    @Register
    fun damage(impactPoint: Vector3, velocity: Vector3)
}
