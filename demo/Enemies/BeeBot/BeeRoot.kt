package Enemies.BeeBot

import godot.annotation.Export
import godot.annotation.Script
import godot.annotation.Register
import godot.api.AnimationNodeStateMachinePlayback
import godot.api.AnimationTree
import godot.api.Node
import godot.api.Node3D
import godot.core.asStringName

@Script
class BeeRoot : Node3D() {

    @Export
    lateinit var stateMachine: AnimationTree

    @Export
    lateinit var beeBot: Node

    private val idleName = "idle".asStringName()
    private val attackName = "spit_attack".asStringName()
    private val powerOffName = "power_off".asStringName()

    private val playbackName = "parameters/StateMachine/playback".asStringName()
    private val surfaceMaterialName1 = "surface_material_override/1".asStringName()
    private val surfaceMaterialName2 = "surface_material_override/2".asStringName()
    private val surfaceMaterialName3 = "surface_material_override/3".asStringName()

    private lateinit var animationPlayback: AnimationNodeStateMachinePlayback

    override fun _ready() {
        animationPlayback = stateMachine.get(playbackName) as AnimationNodeStateMachinePlayback
        stateMachine.active = true
        playIdle()
    }

    @Register
    fun playIdle() {
        animationPlayback.travel(idleName)
    }

    @Register
    fun playSpitAttack() {
        animationPlayback.travel(attackName)
    }

    @Register
    fun playPoweroff() {
        animationPlayback.travel(powerOffName)
    }

    override fun _exitTree() {
        beeBot.set(surfaceMaterialName1, null)
        beeBot.set(surfaceMaterialName2, null)
        beeBot.set(surfaceMaterialName3, null)
    }
}
