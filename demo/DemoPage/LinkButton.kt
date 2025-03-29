package DemoPage

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.api.OS
import godot.api.TextureButton

@RegisterClass
class LinkButton : TextureButton() {

    @Export
    @RegisterProperty
    var link = ""

    @RegisterFunction
    override fun _ready() {
        pressed.connect(this, LinkButton::onButtonPressed)
    }

    @RegisterFunction
    fun onButtonPressed() {
        OS.shellOpen(link)
    }
}