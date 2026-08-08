package DemoPage

import godot.annotation.Export
import godot.annotation.Script
import godot.annotation.Register
import godot.api.OS
import godot.api.TextureButton
import godot.extension.connectMethod

@Script
class LinkButton : TextureButton() {

    @Export
    var link = ""

    override fun _ready() {
        pressed.connectMethod(this, LinkButton::onButtonPressed)
    }

    @Register
    fun onButtonPressed() {
        OS.shellOpen(link)
    }
}
