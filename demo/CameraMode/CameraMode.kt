package CameraMode

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.api.Camera3D
import godot.api.Input
import godot.api.InputEvent
import godot.api.InputEventKey
import godot.api.Node3D
import godot.api.OS
import godot.core.Basis
import godot.core.Key
import godot.core.Vector3
import godot.core.asStringName
import godot.global.GD
import kotlin.math.PI

private const val CAMERA_MODE_TOGGLE_GROUP = "camera_mode_toggle"

@RegisterClass
class CameraMode : Node3D() {

    @Export
    @RegisterProperty
    var cameraSpeed: Int = 10

    @Export
    @RegisterProperty
    var mouseSensitivity: Float = 0.01f

    private var camera: Camera3D? = null
    private var cachedCamera: Camera3D? = null
    private var isEnabled: Boolean = false

    @RegisterFunction
    override fun _ready() {
        isEnabled = OS.isDebugBuild()
        setProcess(isEnabled)
        setProcessInput(isEnabled)
    }

    @RegisterFunction
    override fun _input(event: InputEvent?) {
        if (event is InputEventKey) {
            if (event.isPressed() && !event.isEcho()) {
                if (event.keycode == Key.KEY_F10) {
                    toggleCameraMode()
                }
            }
        }
    }

    @RegisterFunction
    override fun _process(delta: Double) {
        if (!visible) return

        var movement = Vector3.ZERO
        movement += if (Input.isKeyPressed(Key.KEY_W)) {
            Vector3.FORWARD
        } else {
            Vector3.ZERO
        }
        movement += if (Input.isKeyPressed(Key.KEY_A)) {
            Vector3.LEFT
        } else {
            Vector3.ZERO
        }
        movement += if (Input.isKeyPressed(Key.KEY_S)) {
            Vector3.BACK
        } else {
            Vector3.ZERO
        }
        movement += if (Input.isKeyPressed(Key.KEY_D)) {
            Vector3.RIGHT
        } else {
            Vector3.ZERO
        }
        movement += if (Input.isKeyPressed(Key.KEY_Q)) {
            Vector3.DOWN
        } else {
            Vector3.ZERO
        }
        movement += if (Input.isKeyPressed(Key.KEY_E)) {
            Vector3.UP
        } else {
            Vector3.ZERO
        }

        val rotationInput = -Input.getLastMouseVelocity().x * mouseSensitivity
        val tiltInput = -Input.getLastMouseVelocity().y * mouseSensitivity

        camera?.let { camera3D ->
            camera3D.globalTransformMutate {
                with(basis.getEuler()) {
                    x += tiltInput * delta
                    x = GD.clamp(x, -PI + 0.01, PI - 0.01)
                    y += rotationInput * delta
                    basis = Basis.fromEuler(this)
                }
            }
            camera3D.globalPosition += camera3D.globalTransform.basis * movement * delta * cameraSpeed
        }
    }

    private fun toggleCameraMode() {
        if (visible) {
            getTree()?.let { tree -> tree.paused = false }
            cachedCamera?.let { camera3D -> camera3D.current = true }
            camera?.queueFree()
            hide()

            getTree()
                ?.getNodesInGroup(CAMERA_MODE_TOGGLE_GROUP.asStringName())
                ?.filterIsInstance<Node3D>()
                ?.forEach { node ->
                    node.show()
                }
        } else {
            getTree()?.let { tree -> tree.paused = true }
            cachedCamera = getViewport()?.getCamera3d()
            camera = Camera3D()
                .also { newCamera -> addChild(newCamera) }
                .apply {
                    current = true
                    cachedCamera?.fov?.let { this.fov = it }
                    cachedCamera?.globalTransform?.let { this.globalTransform = it }
                }
            show()

            getTree()
                ?.getNodesInGroup(CAMERA_MODE_TOGGLE_GROUP.asStringName())
                ?.filterIsInstance<Node3D>()
                ?.forEach { node ->
                    node.hide()
                }
        }
    }
}