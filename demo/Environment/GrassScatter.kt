package Environment

import godot.annotation.Export
import godot.annotation.RegisterClass
import godot.annotation.RegisterProperty
import godot.api.ArrayMesh
import godot.api.MeshDataTool
import godot.api.MeshInstance3D
import godot.api.MultiMeshInstance3D
import godot.core.Basis
import godot.core.Transform3D
import godot.core.Vector3
import godot.core.times
import godot.global.GD

@RegisterClass
class GrassScatter : MultiMeshInstance3D() {

    @Export
    @RegisterProperty
    lateinit var targetMeshNode: MeshInstance3D


    override fun _ready() {
        val mesh = targetMeshNode.mesh as ArrayMesh
        val mdt = MeshDataTool()
        mdt.createFromSurface(mesh, 0)

        val triangles = mutableListOf<Int>()

        for (i in 0 until mdt.getFaceCount()) {
            val normal = mdt.getFaceNormal(i)
            if (normal.dot(Vector3.UP) < 0.99) continue

            val v1 = mdt.getVertexColor(mdt.getFaceVertex(i, 0))
            val v2 = mdt.getVertexColor(mdt.getFaceVertex(i, 1))
            val v3 = mdt.getVertexColor(mdt.getFaceVertex(i, 2))
            val redness = (v1.r + v2.r + v3.r) / 3.0
            if (redness > 0.25) continue
            triangles.add(i)
        }

        val triangleCount = triangles.size

        val cumulatedTriangleAreas = Array(triangleCount) { 0.0 }

        for (i in 0 until triangleCount) {
            val triangle = mdt.getTriangleVertices(triangles[i])
            val tArea = triangleArea(triangle[0], triangle[1], triangle[2])
            cumulatedTriangleAreas[i] = cumulatedTriangleAreas.getOrElse(i - 1) { 0.0 } + tArea
        }

        val count = 600
        multimesh?.instanceCount = count

        for (i in 0 until count) {
            val t = Transform3D(
                Basis(Vector3.UP, 0.0),
                mdt.getRandomPoint(triangles) + toLocal(targetMeshNode.globalTransform.origin)
            ).scaled(Vector3.ONE * GD.randfRange(0.6f, 0.1f))
            multimesh?.setInstanceTransform(i, t)
        }
    }

    private fun MeshDataTool.getRandomPoint(triangles: List<Int>): Vector3 {
        val chosenTriangle = getTriangleVertices(triangles.random())
        return randomTrianglePoint(chosenTriangle[0], chosenTriangle[1], chosenTriangle[2])
    }

    private fun MeshDataTool.getTriangleVertices(triangleIndex: Int): List<Vector3> {
        val a = getVertex(getFaceVertex(triangleIndex, 0))
        val b = getVertex(getFaceVertex(triangleIndex, 1))
        val c = getVertex(getFaceVertex(triangleIndex, 2))
        return listOf(a, b, c)
    }

    private fun triangleArea(p1: Vector3, p2: Vector3, p3: Vector3): Double {
        return (p2 - p1).cross(p3 - p1).length() / 2.0
    }

    private fun randomTrianglePoint(a: Vector3, b: Vector3, c: Vector3): Vector3 {
        val u = GD.sqrt(GD.randf())
        val v = GD.randf()
        return (1.0 - u) * a + u * (1.0 - v) * b + u * v * c
    }
}
