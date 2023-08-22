package ai.dragonfly.mesh.io

import ai.dragonfly.math.vector.*
import Vec.*
import ai.dragonfly.mesh.sRGB.*
import ai.dragonfly.mesh.Mesh

import narr.*
import Extensions.given
import scala.language.implicitConversions

import java.io.PrintWriter
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("PLY") @JSExportAll
object PLY {

  val defaultComment:String = "Generated by mesh: https://github.com/dragonfly-ai/mesh"

  val defaultHeader = s"ply\nformat ascii 1.0\ncomment $defaultComment"

  val alphaMask:Int = 0xff << 24

  val randomVertexColorMapper: Vec[3] => ARGB32 = (v:Vec[3]) => ARGB32(alphaMask | scala.util.Random.nextInt())

  def writeMesh(mesh: Mesh, out: java.io.OutputStream, vertexColorMapper: Vec[3] => ARGB32): Unit = {
    out.write(fromMesh(mesh, vertexColorMapper).getBytes)
  }

  def fromMesh(mesh: Mesh, vertexColorMapper: Vec[3] => ARGB32):String = {
    val sb: StringBuilder = new StringBuilder()

    sb.append(
s"""$defaultHeader
element vertex ${mesh.points.length}
property float x
property float y
property float z
property uchar red
property uchar green
property uchar blue
property uchar alpha
element face ${mesh.triangles.length}
property list uchar uint vertex_indices
end_header
"""
    )

    var i:Int = 0; while (i < mesh.points.length) {
      val v:Vec[3] = mesh.points(i)
      val c: ARGB32 = vertexColorMapper(v)
      sb.append(s"${v.x} ${v.y} ${v.z} ${c.red} ${c.green} ${c.blue} ${c.alpha}\n")
      i += 1
    }

    for (triangle <- mesh.triangles) {
      sb.append(s"3 ${triangle.v1} ${triangle.v2} ${triangle.v3}\n")
    }

    sb.toString()
  }


  def fromMesh(mesh: Mesh): String = {
    val sb: StringBuilder = new StringBuilder()

    sb.append(
      s"""$defaultHeader
element vertex ${mesh.points.length}
property float x
property float y
property float z
element face ${mesh.triangles.length}
property list uchar uint vertex_indices
end_header
"""
    )

    var i:Int = 0; while (i < mesh.points.length) {
      val v: Vec[3] = mesh.points(i)
//      if (v == null) println(s"$i -> null")
//      else
      sb.append(s"${v.x} ${v.y} ${v.z}\n")
      i += 1
    }

    for (triangle <- mesh.triangles) {
      sb.append(s"3 ${triangle.v1} ${triangle.v2} ${triangle.v3}\n")
    }

    sb.toString()
  }


}
