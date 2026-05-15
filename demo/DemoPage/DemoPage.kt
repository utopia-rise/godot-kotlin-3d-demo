package DemoPage

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.api.Button
import godot.api.Control
import godot.api.GridContainer
import godot.api.Input
import godot.api.InputEvent
import godot.api.Node
import godot.core.Color
import godot.core.asNodePath
import godot.core.asStringName
import godot.core.methodCallable0
import godot.extension.connectLambda
import godot.extension.connectMethod

@RegisterClass
class DemoPage : Node() {
    enum class InstructionType {
        KEYBOARD,
        JOYPAD
    }

    @Export
    @RegisterProperty
    lateinit var demoPageRoot: Control

    @Export
    @RegisterProperty
    lateinit var resumeButton: Button

    @Export
    @RegisterProperty
    lateinit var exitButton: Button

    @Export
    @RegisterProperty
    lateinit var keyboardButton: Button

    @Export
    @RegisterProperty
    lateinit var joypadButton: Button

    @Export
    @RegisterProperty
    lateinit var gridContainerKeyboard: GridContainer

    @Export
    @RegisterProperty
    lateinit var gridContainerJoypad: GridContainer

    private var demoMouseMode: Input.MouseMode = Input.MouseMode.VISIBLE

    @RegisterFunction
    override fun _ready() {
        val tree = getTree() ?: return
        tree.paused = true
        demoMouseMode = Input.getMouseMode()
        Input.setMouseMode(Input.MouseMode.VISIBLE)

        resumeButton.pressed.connectMethod(this, DemoPage::resumeDemo)
        exitButton.pressed.connectLambda { getTree()?.quit(0) }
        keyboardButton.pressed.connectLambda { changeInstruction(InstructionType.KEYBOARD.ordinal) }
        joypadButton.pressed.connectLambda { changeInstruction(InstructionType.JOYPAD.ordinal) }

        changeInstruction(
            if (Input.getConnectedJoypads().isNotEmpty()) {
                InstructionType.JOYPAD.ordinal
            } else {
                InstructionType.KEYBOARD.ordinal
            }
        )
    }

    @RegisterFunction
    override fun _input(event: InputEvent) {
        if (event.isActionPressed("pause".asStringName()) && !event.isEcho()) {
            if (getTree()?.paused == true) {
                resumeDemo()
            } else {
                pauseDemo()
            }
        }
    }

    @RegisterFunction
    fun changeInstruction(type: Int) {
        when (type) {
            InstructionType.KEYBOARD.ordinal -> {
                keyboardButton.modulateMutate { a = 1.0 }
                joypadButton.modulateMutate { a = 0.3 }
                gridContainerKeyboard.show()
                gridContainerJoypad.hide()
            }

            InstructionType.JOYPAD.ordinal -> {
                keyboardButton.modulateMutate { a = 0.3 }
                joypadButton.modulateMutate { a = 1.0 }
                gridContainerKeyboard.hide()
                gridContainerJoypad.show()
            }
        }

        keyboardButton.releaseFocus()
        joypadButton.releaseFocus()
    }

    private fun pauseDemo() {
        demoMouseMode = Input.getMouseMode()
        getTree()?.let { it.paused = true }
        demoPageRoot.show()
        createTween().tweenProperty(demoPageRoot, demoPageRoot::modulate.name.asNodePath(), Color.white, 0.3)
        Input.setMouseMode(Input.MouseMode.VISIBLE)
    }

    @RegisterFunction
    fun resumeDemo() {
        getTree()?.let { it.paused = false }
        createTween().apply {
            tweenProperty(demoPageRoot, demoPageRoot::modulate.name.asNodePath(), Color.transparent, 0.3)
            tweenCallback(methodCallable0(demoPageRoot, Control::hide))
        }

        Input.setMouseMode(demoMouseMode)
    }
}
