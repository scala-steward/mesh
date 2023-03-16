package ai.dragonfly.mesh.io

import ai.dragonfly.mesh.*
import ai.dragonfly.math.vector.Vector3
import narr.*

import java.io.{BufferedReader, InputStream, InputStreamReader, OutputStream}
import scala.collection.mutable
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("OBJ") @JSExportAll
object OBJ {

  private val defaultComment = s"OBJ file generated by the mesh.dragonfly.ai Scala library.  Visit http://dragonfly.ai for more information."
  private val defaultMaterialFileName:String = "default.mtl"

  val vertexLine: StringContext = StringContext("v ", " ", " ", "")

  def fromMesh(mesh:Mesh, name:String, comment:String = defaultComment, materialLibraryFile:String = defaultMaterialFileName, material:MTL = MTL.default, smooth:Boolean = true): String = {
    val sb = new mutable.StringBuilder()
    sb.append(s"#  $comment\n")
    sb.append(s"mtllib $materialLibraryFile")
    sb.append(s"o $name\n")
    for (p <- mesh.points) { sb.append(s"v ${p.x} ${p.z} ${p.y}\n") }

    if (smooth) sb.append(s"s 1\n")

    sb.append(s"usemtl ${material.name}\n")

    for (t <- mesh.triangles) {
      if (t != null) sb.append( s"f ${t.v1 + 1} ${t.v2 + 1} ${t.v3 + 1}\n" )
    }

    sb.toString()
  }
  def objTriangle(t:Triangle, offset: Int = 0): String = s"f ${offset + t.v1 + 1} ${offset + t.v2 + 1} ${offset + t.v3 + 1}"

  /* How to add materials to meshes?  How to combine them without losing materials? */
  def fromMaterialMeshGroup(meshGroup:MaterialMeshGroup, comment:String = defaultComment, materialLibraryFile:String = defaultMaterialFileName): String = {
    var pointCount = 0
    var triangleCount = 0

    for (m <- meshGroup.meshes) {
      pointCount = pointCount + m.points.length
      triangleCount = triangleCount + m.triangles.length
    }

    val pointSB = new mutable.StringBuilder()
    val triangleSB = new mutable.StringBuilder()

    pointSB.append(s"# $comment\n\n")
    pointSB.append(s"mtllib $materialLibraryFile\n\n")

    var pi: Int = 0

    var mi:Int = 0; while (mi < meshGroup.meshes.length) {
      var pj = 0
      val m:Mesh = meshGroup.meshes(mi)
      for (p <- m.points) {
        pointSB.append(s"v ${-p.x} ${p.z} ${p.y}\n")
        pj = pj + 1
      }

      var tj = 0

      triangleSB.append(s"g ${m.name}\n")
      //if (m.smooth) triangleSB.append(s"s 1\n") else
      triangleSB.append(s"s 1\n")
      triangleSB.append(s"usemtl ${meshGroup.material.name}\n")

      for (t <- m.triangles) {
        triangleSB.append(s"${objTriangle(t, pi)}\n")
        tj = tj + 1
      }

      pi += m.points.length
      mi += 1
    }
    pointSB.append(triangleSB).toString()
  }

  // IO
  def writeMesh(mesh:Mesh, out:OutputStream, name:String, comment:String = defaultComment, materialLibraryFileName:String = defaultMaterialFileName, material:MTL = MTL.default): Unit = {
    out.write(OBJ.fromMesh(mesh, name, comment, materialLibraryFileName, material).getBytes)
  }

  def writeMaterialMeshGroup(meshGroup:MaterialMeshGroup, out:OutputStream, comment:String = defaultComment, materialLibraryFileName:String = defaultMaterialFileName): Unit = {
    out.write(OBJ.fromMaterialMeshGroup(meshGroup, comment, materialLibraryFileName).getBytes)
  }

  def parseVertex(line:String):Option[Vector3] = {
    vertexLine.s.unapplySeq(line) match {
      case Some(Seq(xS:String, yS:String, zS:String)) =>
        try {
          Some(
            Vector3(
              java.lang.Double.parseDouble(xS),
              java.lang.Double.parseDouble(yS),
              java.lang.Double.parseDouble(zS)
            )
          )
        } catch {
          case _:Throwable => None
        }
      case _ => None
    }
  }

  /**
   * Only parses triangles and quads.
   * @param line a line of text depicting an OBJ face.
   * @return
   */
  def parseFace(line:String):NArray[Triangle] = {
    val tokens:Array[String] = line.split("\\s")
    if (!tokens.head.equals("f")) new NArray[Triangle](0)
    else {
      tokens.length match {
        case 4 =>
          val o = new NArray[Triangle](1)
          o(0) = Triangle(
            Integer.parseInt(tokens(1)) - 1,
            Integer.parseInt(tokens(2)) - 1,
            Integer.parseInt(tokens(3)) - 1
          )
          o
        case 5 => Triangle.fromQuad(
          Integer.parseInt(tokens(1))-1,
          Integer.parseInt(tokens(2))-1,
          Integer.parseInt(tokens(3))-1,
          Integer.parseInt(tokens(4))-1
        )
        case _ => new NArray[Triangle](0)
      }
    }
  }

//  def read(name:String, is:InputStream):OBJ = {
//    val br: java.io.BufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"))
//
//    var line = br.readLine()
//    var points:List[Vector3] = List[Vector3]()
//    var triangles:List[Triangle] = List[Triangle]()
//
//    while (line != null) {
//      parseVertex(line) match {
//        case Some(p: Vector3) =>
//          points = points.appended(p)
//        case None =>
//          parseFace(line) match {
//            case Some(t: Triangle) =>
//              triangles = triangles.appended(t)
//            case _ => // ignore
//          }
//      }
//      line = br.readLine()
//    }
//    br.close()
//    val pointsArr:NArray[Vector3] = NArray.tabulate[Vector3](points.size)((i:Int) => points(i))
//    val triangleArr:NArray[Triangle] = NArray.tabulate[Triangle](triangles.size)((i:Int) => triangles(i))
//
//    MaterialMeshGroup(MTL.default, )
//  }
}

@JSExportTopLevel("MaterialMeshGroup") @JSExportAll
case class MaterialMeshGroup(name:String, material: MTL, meshes: NArray[Mesh])

type OBJ = NArray[MaterialMeshGroup]