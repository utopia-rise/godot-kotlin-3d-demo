package icons

import godot.annotation.RegisterClass
import godot.api.TextureRect
import godot.core.Color
import godot.core.asNodePath

@RegisterClass
class Icone : TextureRect() {

    private val disabledAlpha = 0.2

    override fun _ready() {
        modulate.a = disabledAlpha
    }

    fun setState(state: Boolean) {
        val fromTo = if (state) {
            arrayOf(Color(1, 1, 1, disabledAlpha), Color.white)
        } else {
            arrayOf(Color.white, Color(1, 1, 1, disabledAlpha))
        }

        val tween = createTween()!!
        tween.tweenProperty(this, "modulate".asNodePath(), fromTo[0], 0.2)!!.from(fromTo[1])
    }
}
