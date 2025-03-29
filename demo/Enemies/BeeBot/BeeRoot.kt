package Enemies.BeeBot

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.api.AnimationNodeStateMachinePlayback
import godot.api.AnimationTree
import godot.api.Node
import godot.api.Node3D
import godot.core.asStringName

@RegisterClass
class BeeRoot : Node3D() {

    @Export
    @RegisterProperty
    lateinit var stateMachine: AnimationTree

    @Export
    @RegisterProperty
    lateinit var beeBot: Node

    private val idleName = "idle".asStringName()
    private val attackName = "spit_attack".asStringName()
    private val powerOffName = "power_off".asStringName()

    private val playbackName = "parameters/StateMachine/playback".asStringName()
    private val surfaceMaterialName1 = "surface_material_override/1".asStringName()
    private val surfaceMaterialName2 = "surface_material_override/2".asStringName()
    private val surfaceMaterialName3 = "surface_material_override/3".asStringName()

    private lateinit var animationPlayback: AnimationNodeStateMachinePlayback

    @RegisterFunction
    override fun _ready() {
        animationPlayback = stateMachine.get(playbackName) as AnimationNodeStateMachinePlayback
        stateMachine.active = true
        playIdle()
    }

    @RegisterFunction
    fun playIdle() {
        animationPlayback.travel(idleName)
    }

    @RegisterFunction
    fun playSpitAttack() {
        animationPlayback.travel(attackName)
    }

    @RegisterFunction
    fun playPoweroff() {
        animationPlayback.travel(powerOffName)
    }

    @RegisterFunction
    override fun _exitTree() {
        beeBot.set(surfaceMaterialName1, null)
        beeBot.set(surfaceMaterialName2, null)
        beeBot.set(surfaceMaterialName3, null)
    }
}
