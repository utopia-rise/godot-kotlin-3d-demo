package Enemies

import Enemies.smoke_puff.SmokePuff
import Player.Coin.Coin
import godot.annotation.Export
import godot.annotation.RegisterProperty
import godot.api.PackedScene
import godot.api.ResourceLoader
import godot.api.RigidBody3D
import godot.coroutines.await
import godot.coroutines.awaitMainThread
import godot.coroutines.godotCoroutine
import godot.extension.instantiateAs
import godot.extension.loadAs
import shared.Damageable

abstract class Enemy : RigidBody3D(), Damageable {
    @Export
    @RegisterProperty
    abstract var coinsCount: Int

    private val puffScene = ResourceLoader.loadAs<PackedScene>("res://demo/Enemies/smoke_puff/smoke_puff.tscn")!!
    private val coinScene = ResourceLoader.loadAs<PackedScene>("res://demo/Player/Coin/Coin.tscn")!!

    fun death() {
        val timer = getTree()!!.createTimer(2.0)!!
        godotCoroutine {
            timer.timeout.await()

            val puff = puffScene.instantiateAs<SmokePuff>()!!
            awaitMainThread {
                getParent()?.addChild(puff)
                puff.globalPosition = globalPosition
            }

            val coins = List(coinsCount) {
                coinScene.instantiateAs<Coin>()!!
            }

            awaitMainThread {
                for (coin in coins) {
                    getParent()?.addChild(coin)
                    coin.globalPosition = globalPosition
                    coin.spawn()
                }
            }

            queueFree()
        }
    }
}