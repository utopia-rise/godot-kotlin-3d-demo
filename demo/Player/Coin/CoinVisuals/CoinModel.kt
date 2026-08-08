package Player.Coin.CoinVisuals

import godot.annotation.Export
import godot.annotation.Script
import godot.api.Node3D
import godot.api.Time
import godot.global.GD

@Script
class CoinModel : Node3D() {

    @Export
    var yAmplitude = 0.04

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
