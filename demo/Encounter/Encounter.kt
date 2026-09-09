package Encounter

import Enemies.Enemy
import Player.Coin.Coin
import Player.Player
import godot.annotation.Export
import godot.annotation.Script
import godot.api.Area3D
import godot.api.AudioStreamPlayer
import godot.api.BoxShape3D
import godot.api.CollisionShape3D
import godot.api.Node
import godot.api.Node3D
import godot.api.PackedScene
import godot.api.PhysicsRayQueryParameters3D
import godot.core.Vector3
import godot.coroutines.NodeScope
import godot.core.VariantArray
import godot.coroutines.asFlow
import godot.coroutines.await
import godot.coroutines.awaitNextProcess
import godot.coroutines.launch
import godot.coroutines.offload
import godot.coroutines.threadSafe
import godot.extension.instantiateAs
import godot.extension.getNodeAs
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlin.random.Random

@Script
class Encounter : Area3D() {
    private data class EnemySpawn(
        val sceneIndex: Int,
        val x: Double,
        val z: Double,
    )

    @Export
    var enemyScenes: VariantArray<PackedScene> = VariantArray()

    @Export
    var enemyCount = 4

    @Export
    lateinit var coinScene: PackedScene

    @Export
    lateinit var battleMusic: AudioStreamPlayer

    @Export
    lateinit var encounterShape: CollisionShape3D

    private lateinit var stageMusic: AudioStreamPlayer

    @Export
    lateinit var victoryJingle: AudioStreamPlayer

    override fun _ready() {
        stageMusic = getNodeAs("../StageMusic")!!

        launch {
            battleMusic.finished.asFlow().collect { battleMusic.play() }
        }
        launch {
            runWave()
        }
    }

    private suspend fun NodeScope.runWave() {
        bodyEntered.asFlow<Node3D>().first { it is Player }
        awaitNextProcess()
        val level = checkNotNull(getParent()) { "Encounter needs a parent node." }
        stageMusic.stop()
        battleMusic.play()

        val enemyChoices = enemyScenes.toList()
        val halfSize = (encounterShape.shape as BoxShape3D).size * 0.5
        val spawnPlan = List(enemyCount) {
                EnemySpawn(
                    sceneIndex = Random.nextInt(enemyChoices.size),
                    x = Random.nextDouble(-halfSize.x, halfSize.x),
                    z = Random.nextDouble(-halfSize.z, halfSize.z),
                )
            }

        val defeatedEnemies = spawnEnemies(level, enemyChoices, spawnPlan)
        defeatedEnemies.awaitAll()
        awaitNextProcess()
        battleMusic.stop()
        victoryJingle.play()
        val rewardPosition = groundPositionAt(encounterShape.toGlobal(Vector3.ZERO))
        spawnCoins(level, rewardPosition)
        victoryJingle.finished.await()
        stageMusic.play()
        queueFree()
    }

    private fun NodeScope.spawnEnemies(
        level: Node,
        enemyChoices: List<PackedScene>,
        spawnPlan: List<EnemySpawn>,
    ) = spawnPlan.map { spawn ->
        val enemy = enemyChoices[spawn.sceneIndex].instantiateAs<Enemy>()!!
        enemy.coinsCount = 0
        level.addChild(enemy)
        enemy.globalPosition = groundPositionAt(
            encounterShape.toGlobal(Vector3(spawn.x, 0.0, spawn.z)),
        )

        async { enemy.treeExiting.await() }
    }

    private fun spawnCoins(level: Node, position: Vector3) {
        repeat(15) {
            val coin = coinScene.instantiateAs<Coin>()!!
            level.addChild(coin)
            coin.globalPosition = position
            coin.spawn()
        }
    }

    private fun groundPositionAt(point: Vector3): Vector3 {
        val query = PhysicsRayQueryParameters3D.create(
            point + Vector3.UP * 20.0,
            point - Vector3.UP * 40.0,
        )!!
        return getWorld3d()!!.directSpaceState!!
            .intersectRay(query)["position"] as? Vector3
            ?: error("Encounter needs ground collision beneath its area.")
    }
}
