package Level

import Player.Player
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.api.Area3D
import godot.api.Node3D
import godot.extension.connectLambda

@RegisterClass
class DeathPlane : Area3D() {

    @RegisterFunction
    override fun _ready() {
        bodyEntered.connectLambda { body: Node3D ->
            if (body is Player) {
                body.resetPosition()
            }
        }
    }
}
