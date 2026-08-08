package icons

import godot.annotation.Export
import godot.annotation.Script
import godot.annotation.Register
import godot.api.PanelContainer

@Script
class WeaponUI : PanelContainer() {

    @Export
    lateinit var flashNode: Icone

    @Export
    lateinit var grenadeNode: Icone

    val nodes = mutableMapOf<String, Icone>()
    var selectedNode: String = ""

    override fun _ready() {
        nodes["DEFAULT"] = flashNode
        nodes["GRENADE"] = grenadeNode
    }

    @Register
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
