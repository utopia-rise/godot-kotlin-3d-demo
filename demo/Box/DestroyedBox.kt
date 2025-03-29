package Box

import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.api.Node3D
import godot.api.RigidBody3D
import godot.core.Vector3
import kotlin.random.Random

const val FLYING_PIECES = 3
const val THROW_STRENGTH = 500.0f

@RegisterClass
class DestroyedBox : Node3D() {
    private val _piecesIdx = mutableListOf(0, 1, 2, 3, 4, 5)

    @RegisterFunction
    override fun _ready() {
        _piecesIdx.shuffle()

        for (i in 0 until FLYING_PIECES) {
            val pieceIdx = _piecesIdx[i]
            val piece = getChild(pieceIdx) as RigidBody3D
            piece.show()
            piece.freeze = false
            piece.sleeping = false
            piece.setCollisionMaskValue(1, true)

            val randVector = (Vector3.ONE * 0.5) - Vector3(Random.nextFloat(), Random.nextFloat(), Random.nextFloat())

            piece.applyForce(randVector * THROW_STRENGTH, randVector)
        }
    }
}
