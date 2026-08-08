package Player

import godot.annotation.Export
import godot.annotation.Script
import godot.annotation.Register
import godot.annotation.Emit
import godot.api.AnimationNodeOneShot
import godot.api.AnimationNodeStateMachinePlayback
import godot.api.AnimationPlayer
import godot.api.AnimationTree
import godot.api.Node3D
import godot.core.Signal0
import godot.core.asStringName
import godot.core.signal0

@Script
class CharacterSkin : Node3D() {

    @Emit
    val footStep: Signal0 by signal0()

    @Export
    lateinit var mainAnimationPlayer: AnimationPlayer

    @Export
    lateinit var animationTree: AnimationTree

    private val stateMachine: AnimationNodeStateMachinePlayback by lazy {
        animationTree.get("parameters/StateMachine/playback".asStringName()) as AnimationNodeStateMachinePlayback
    }

    private var movingBlendPath = "parameters/StateMachine/move/blend_position".asStringName()
    private var punchOneShotPath = "parameters/PunchOneShot/request".asStringName()
    private var idleAnimation = "idle".asStringName()
    private var moveAnimation = "move".asStringName()
    private var jumpAnimation = "jump".asStringName()
    private var fallAnimation = "fall".asStringName()

    override fun _ready() {
        animationTree.active = true
        mainAnimationPlayer.playbackDefaultBlendTime = 0.1
    }

    @Register
    fun setMoving(isMoving: Boolean) {
        if (isMoving) {
            stateMachine.travel(moveAnimation)
        } else {
            stateMachine.travel(idleAnimation)
        }
    }

    @Register
    fun setMovingSpeed(speed: Double) {
        animationTree.set(movingBlendPath, speed)
    }

    @Register
    fun jump() {
        stateMachine.travel(jumpAnimation)
    }

    @Register
    fun fall() {
        stateMachine.travel(fallAnimation)
    }

    @Register
    fun punch() {
        animationTree.set(punchOneShotPath, AnimationNodeOneShot.OneShotRequest.FIRE.value)
    }
}
