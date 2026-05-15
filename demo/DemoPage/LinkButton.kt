package DemoPage

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.api.OS
import godot.api.TextureButton
import godot.extension.connectMethod

@RegisterClass
class LinkButton : TextureButton() {

    @Export
    @RegisterProperty
    var link = ""

    @RegisterFunction
    override fun _ready() {
        pressed.connectMethod(this, LinkButton::onButtonPressed)
    }

    @RegisterFunction
    fun onButtonPressed() {
        OS.shellOpen(link)
    }
}
