import godot.annotation.RegisterClass
import godot.api.Input
import godot.api.InputEvent
import godot.api.InputEventKey
import godot.api.InputEventMouseButton
import godot.api.Node
import godot.api.OS
import godot.api.Window
import godot.core.Key

@RegisterClass
class FullScreenHandler : Node() {

    init {
        processMode = ProcessMode.ALWAYS
    }

    override fun _input(event: InputEvent?) {
        if (OS.hasFeature("HTML5")) {
            if (event is InputEventMouseButton && Input.getMouseMode() != Input.MouseMode.CAPTURED) {
                Input.setMouseMode(Input.MouseMode.CAPTURED)
            }
        } else {
            if (event is InputEventKey && event.isPressed()) {
                if (event.keycode == Key.F11 || (event.keycode == Key.ENTER && event.altPressed)) {
                    getTree()?.root?.mode = if (getTree()?.root?.mode == Window.Mode.FULLSCREEN) {
                        Window.Mode.WINDOWED
                    } else {
                        Window.Mode.FULLSCREEN
                    }
                }
            }
        }
    }
}
