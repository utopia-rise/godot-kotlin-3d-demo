package Level

import Player.Player
import godot.annotation.Script
import godot.api.Area3D
import godot.api.Node3D
import godot.extension.connectLambda

@Script
class DeathPlane : Area3D() {

    override fun _ready() {
        bodyEntered.connectLambda { body: Node3D ->
            if (body is Player) {
                body.resetPosition()
            }
        }
    }
}
