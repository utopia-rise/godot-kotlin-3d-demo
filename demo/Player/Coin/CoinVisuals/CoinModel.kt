package Player.Coin.CoinVisuals

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.api.Node3D
import godot.api.Time
import godot.global.GD

@RegisterClass
class CoinModel : Node3D() {

    @Export
    @RegisterProperty
    var yAmplitude = 0.04

    @RegisterFunction
    override fun _process(delta: Double) {
        val t = Time.getTicksMsec().toDouble() / 1000.0
        rotationMutate {
            y += 1.50 * delta
        }
        positionMutate {
            y = GD.sin(t) * yAmplitude
        }
    }
}
