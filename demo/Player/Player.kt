package Player

import Player.Coin.Coin
import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.annotation.RegisterSignal
import godot.api.AnimationPlayer
import godot.api.AudioStreamPlayer3D
import godot.api.CharacterBody3D
import godot.api.ColorRect
import godot.api.Input
import godot.api.InputEventKey
import godot.api.InputEventMouseButton
import godot.api.InputMap
import godot.api.Node3D
import godot.api.PackedScene
import godot.api.ShapeCast3D
import godot.core.Basis
import godot.core.Key
import godot.core.MouseButton
import godot.core.Vector3
import godot.core.asCachedStringName
import godot.core.asStringName
import godot.core.signal1
import godot.global.GD
import shared.Damageable

enum class WeaponType {
    DEFAULT,
    GRENADE
}

@RegisterClass
class Player : CharacterBody3D(), Damageable {
    @RegisterSignal("weapon_name")
    val weaponSwitched by signal1<String>()

    @Export
    @RegisterProperty
    lateinit var bulletScene: PackedScene

    @Export
    @RegisterProperty
    lateinit var coinScene: PackedScene

    @Export
    @RegisterProperty
    var moveSpeed = 8.0

    @Export
    @RegisterProperty
    var bulletSpeed = 10.0

    @Export
    @RegisterProperty
    var attackImpulse = 10.0

    @Export
    @RegisterProperty
    var acceleration = 4.0

    @Export
    @RegisterProperty
    var jumpInitialImpulse = 12.0

    @Export
    @RegisterProperty
    var jumpAdditionalForce = 4.5

    @Export
    @RegisterProperty
    var rotationSpeed = 12.0

    @Export
    @RegisterProperty
    var stoppingSpeed = 1.0

    @Export
    @RegisterProperty
    var maxThrowbackForce = 15.0

    @Export
    @RegisterProperty
    var shootCooldown = 0.5

    @Export
    @RegisterProperty
    var grenadeCooldown = 0.5

    @Export
    @RegisterProperty
    lateinit var rotationRoot: Node3D

    @Export
    @RegisterProperty
    lateinit var cameraController: CameraController

    @Export
    @RegisterProperty
    lateinit var attackAnimationPlayer: AnimationPlayer

    @Export
    @RegisterProperty
    lateinit var groundShapecast: ShapeCast3D

    @Export
    @RegisterProperty
    lateinit var grenadeAimController: GrenadeLauncher

    @Export
    @RegisterProperty
    lateinit var characterSkin: CharacterSkin

    @Export
    @RegisterProperty
    lateinit var uiAimRecticle: ColorRect

    @Export
    @RegisterProperty
    lateinit var uiCoinsContainer: CoinsContainer

    @Export
    @RegisterProperty
    lateinit var stepSound: AudioStreamPlayer3D

    @Export
    @RegisterProperty
    lateinit var landingSound: AudioStreamPlayer3D

    @RegisterProperty
    var groundHeight = 0.0

    private var equipedWeapon = WeaponType.DEFAULT
    private var moveDirection = Vector3.ZERO
    private var lastStrongDirection = Vector3.FORWARD
    private val gravity = -30.0
    private lateinit var startPosition: Vector3
    private var coins = 0
    private var isOnFloorBuffer = false

    private var shootCooldownTick = shootCooldown
    private var grenadeCooldownTick = grenadeCooldown

    @RegisterFunction
    override fun _ready() {
        startPosition = globalPosition
        Input.setMouseMode(Input.MouseMode.CAPTURED)
        cameraController.setup(this)
        grenadeAimController.visible = false
        weaponSwitched.emit(WeaponType.DEFAULT.name)

        // When copying this character to a new project, the project may lack required input actions.
        // In that case, we register input actions for the user at runtime.
        if (!InputMap.hasAction("move_left".asStringName())) {
            registerInputActions()
        }
    }

    @RegisterFunction
    override fun _physicsProcess(delta: Double) {
        // Calculate ground height for camera controller
        if (groundShapecast.getCollisionCount() > 0) {
            (0 until groundShapecast.getCollisionCount()).forEach { collisionIndex ->
                val collisionPoint = groundShapecast.getCollisionPoint(collisionIndex)
                groundHeight = GD.max(groundHeight, collisionPoint.y)
            }
        } else {
            groundHeight = globalPosition.y + groundShapecast.targetPosition.y
        }

        if (globalPosition.y < groundHeight) {
            groundHeight = globalPosition.y
        }

        // Swap weapons
        if (Input.isActionJustPressed("swap_weapons".asCachedStringName())) {
            equipedWeapon = when (equipedWeapon) {
                WeaponType.DEFAULT -> WeaponType.GRENADE
                WeaponType.GRENADE -> WeaponType.DEFAULT
            }
            grenadeAimController.visible = equipedWeapon == WeaponType.GRENADE
            weaponSwitched.emit(equipedWeapon.name)
        }

        // Get input and movement state
        val isAttacking = Input.isActionPressed("attack".asStringName()) && !attackAnimationPlayer.isPlaying()
        val isJustAttacking = Input.isActionJustPressed("attack".asStringName())
        val isJustJumping = Input.isActionJustPressed("jump".asStringName()) && isOnFloor()
        val isAiming = Input.isActionPressed("aim".asStringName()) && isOnFloor()
        val isAirBoosting = Input.isActionPressed("jump".asStringName()) && !isOnFloor() && velocity.y > 0.0
        val isJustOnFloor = isOnFloor() && !isOnFloorBuffer

        isOnFloorBuffer = isOnFloor()
        moveDirection = getCameraOrientedInput()

        // To not orient quickly to the last input, we save a last strong direction,
        // this also ensures a good normalized value for the rotation basis.
        if (moveDirection.length() > 0.2) {
            lastStrongDirection = moveDirection.normalized()
        }

        if (isAiming) {
            lastStrongDirection = (cameraController.globalTransform.basis * Vector3.BACK).normalized()
        }

        orientCharacterToDirection(lastStrongDirection, delta)

        // We separate out the y velocity to not interpolate on the gravity
        val yVelocity = velocity.y
        velocityMutate { y = 0.0 }
        velocity = velocity.lerp(moveDirection * moveSpeed, acceleration * delta)
        if (moveDirection.length() == 0.0 && velocity.length() < stoppingSpeed) {
            velocity = Vector3.ZERO
        }
        velocityMutate { y = yVelocity }

        // Set aiming camera and UI
        if (isAiming) {
            cameraController.setPivot(CameraController.CameraPivot.OVER_SHOULDER.ordinal)
            grenadeAimController.throwDirection = cameraController.camera.quaternion * Vector3.FORWARD
            grenadeAimController.fromLookPosition = cameraController.camera.globalPosition
            uiAimRecticle.visible = true
        } else {
            cameraController.setPivot(CameraController.CameraPivot.THIRD_PERSON.ordinal)
            grenadeAimController.throwDirection = lastStrongDirection
            grenadeAimController.fromLookPosition = globalPosition
            uiAimRecticle.visible = false
        }

        // Update attack state and position
        shootCooldownTick += delta
        grenadeCooldownTick += delta

        if (isAttacking) {
            when (equipedWeapon) {
                WeaponType.DEFAULT -> if (isAiming && isOnFloor()) {
                    if (shootCooldownTick > shootCooldown) {
                        shootCooldownTick = 0.0
                        shoot()
                    }
                } else if (isJustAttacking) {
                    attack()
                }

                WeaponType.GRENADE -> if (grenadeCooldownTick > grenadeCooldown) {
                    grenadeCooldownTick = 0.0
                    grenadeAimController.throwGrenade()
                }
            }
        }

        velocityMutate { y += gravity * delta }

        if (isJustJumping) {
            velocityMutate { y += jumpInitialImpulse }
        } else if (isAirBoosting) {
            velocityMutate { y += jumpAdditionalForce * delta }
        }

        // Set character animation
        when {
            isJustJumping -> characterSkin.jump()
            !isOnFloor() && velocity.y < 0 -> characterSkin.fall()
            isOnFloor() -> {
                val xzVelocity = Vector3(velocity.x, 0, velocity.z)
                if (xzVelocity.length() > stoppingSpeed) {
                    characterSkin.setMoving(true)
                    characterSkin.setMovingSpeed(GD.inverseLerp(0.0, moveSpeed, xzVelocity.length()))
                } else {
                    characterSkin.setMoving(false)
                }
            }
        }

        if (isJustOnFloor) {
            landingSound.play()
        }

        val positionBefore = globalPosition
        moveAndSlide()
        val positionAfter = globalPosition

        // If velocity is not 0 but the difference of positions after move_and_slide is,
        // character might be stuck somewhere!
        val deltaPosition = positionAfter - positionBefore
        val epsilon = 0.001
        if (deltaPosition.length() < epsilon && velocity.length() > epsilon) {
            globalPosition += getWallNormal() * 0.1
        }
    }

    private fun attack() {
        attackAnimationPlayer.play("Attack".asStringName())
        characterSkin.punch()
        velocity = rotationRoot.transform.basis * Vector3.BACK * attackImpulse
    }

    private fun shoot() {
        val bullet = bulletScene.instantiate() as Bullet
        bullet.shooter = this
        val origin = globalPosition + Vector3.UP
        val aimTarget = cameraController.getAimTarget()
        val aimDirection = (aimTarget - origin).normalized()
        bullet.velocity = aimDirection * bulletSpeed
        bullet.distanceLimit = 14f
        getParent()?.addChild(bullet)
        bullet.globalPosition = origin
    }

    @RegisterFunction
    fun resetPosition() = transformMutate {
        origin = startPosition
    }

    @RegisterFunction
    fun collectCoin() {
        coins += 1
        uiCoinsContainer.updateCoinsAmount(coins)
    }

    @RegisterFunction
    fun looseCoins() {
        val lostCoins = GD.min(coins, 5)
        coins -= lostCoins
        repeat(lostCoins) {
            val coin = coinScene.instantiate() as Coin
            getParent()?.addChild(coin)
            coin.globalPosition = globalPosition
            coin.spawn()
        }
        uiCoinsContainer.updateCoinsAmount(coins)
    }

    private fun getCameraOrientedInput(): Vector3 {
        if (attackAnimationPlayer.isPlaying()) return Vector3.ZERO

        val rawInput = Input.getVector(
            "move_left".asStringName(),
            "move_right".asStringName(),
            "move_up".asStringName(),
            "move_down".asStringName()
        )

        var input = Vector3.ZERO
        // This is to ensure that diagonal input isn't stronger than axis aligned input
        input.x = -rawInput.x * GD.sqrt(1.0 - rawInput.y * rawInput.y / 2.0)
        input.z = -rawInput.y * GD.sqrt(1.0 - rawInput.x * rawInput.x / 2.0)

        input = cameraController.globalTransform.basis * input
        input.y = 0.0
        return input
    }

    @RegisterFunction
    fun playFootStepSound() {
        stepSound.pitchScale = GD.randfn(1.2f, 0.2f)
        stepSound.play()
    }

    @RegisterFunction
    override fun damage(impactPoint: Vector3, force: Vector3) {
        // Always throws character up
        force.y = GD.abs(force.y)
        velocity = force.limitLength(maxThrowbackForce)
        looseCoins()
    }

    private fun orientCharacterToDirection(direction: Vector3, delta: Double) {
        val leftAxis = Vector3.UP.cross(direction)
        val rotationBasis = Basis(leftAxis, Vector3.UP, direction).getRotationQuaternion()
        val modelScale = rotationRoot.transform.basis.getScale()

        val newBasis = Basis(
            rotationRoot
                .transform
                .basis
                .getRotationQuaternion()
                .slerp(rotationBasis, delta * rotationSpeed)
        ).scaled(modelScale)

        rotationRoot.transformMutate { basis = newBasis }
    }

    // Used to register required input actions when copying this character to a different project.
    private fun registerInputActions() {
        val inputKeyActions = mapOf(
            "move_left".asStringName() to Key.A,
            "move_right".asStringName() to Key.D,
            "move_up".asStringName() to Key.W,
            "move_down".asStringName() to Key.S,
            "jump".asStringName() to Key.SPACE,
            "swap_weapons".asStringName() to Key.TAB,
            "pause".asStringName() to Key.ESCAPE,
            "camera_left".asStringName() to Key.Q,
            "camera_right".asStringName() to Key.E,
            "camera_up".asStringName() to Key.R,
            "camera_down".asStringName() to Key.F,
        )

        val inputMouseActions = mapOf(
            "attack".asStringName() to MouseButton.LEFT,
            "aim".asStringName() to MouseButton.RIGHT,
        )

        inputKeyActions.forEach { (action, key) ->
            if (!InputMap.hasAction(action)) {
                InputMap.addAction(action)
                val inputKey = InputEventKey().apply { keycode = key }
                InputMap.actionAddEvent(action, inputKey)
            }
        }

        inputMouseActions.forEach { (action, button) ->
            if (!InputMap.hasAction(action)) {
                InputMap.addAction(action)
                val inputKey = InputEventMouseButton().apply { buttonIndex = button }
                InputMap.actionAddEvent(action, inputKey)
            }
        }
    }
}