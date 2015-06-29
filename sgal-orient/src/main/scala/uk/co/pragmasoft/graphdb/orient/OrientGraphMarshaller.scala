package uk.co.pragmasoft.graphdb.orient

import uk.co.pragmasoft.graphdb.marshalling.GraphMarshaller

trait OrientGraphMarshaller[T] extends GraphMarshaller[T] {
  /**
   * The class name for this vertex in Orient
   */
  def vertexClassName: String

  lazy val vertexClassSpec: String = s"class:$vertexClassName"
}
