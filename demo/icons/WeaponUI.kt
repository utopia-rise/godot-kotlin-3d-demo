package icons

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.annotation.RegisterProperty
import godot.api.PanelContainer

@RegisterClass
class WeaponUI : PanelContainer() {

    @Export
    @RegisterProperty
    lateinit var flashNode: Icone

    @Export
    @RegisterProperty
    lateinit var grenadeNode: Icone

    val nodes = mutableMapOf<String, Icone>()
    var selectedNode: String = ""

    @RegisterFunction
    override fun _ready() {
        nodes["DEFAULT"] = flashNode
        nodes["GRENADE"] = grenadeNode
    }

    @RegisterFunction
    fun switchTo(nodeName: String) {
        // Return if same node
        if (nodeName == selectedNode) return
        if (selectedNode.isNotEmpty()) {
            // Unselect previous
            nodes[selectedNode]?.setState(false)
        }
        // Select node
        nodes[nodeName]?.setState(true)
        selectedNode = nodeName
    }
}
