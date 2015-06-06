package uk.co.pragmasoft.graphdb.orient

import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.impls.orient.{OrientBaseGraph, OrientGraph, OrientVertex}

trait OrientIndexNamingSupport {
  protected def edgePropertyName(edgeClassName: String, direction: Direction)(implicit graph: OrientGraph): String = {
    val clsName: String = OrientBaseGraph.encodeClassName(edgeClassName)
    val useVertexFieldsForEdgeLabels = graph.isUseVertexFieldsForEdgeLabels
    OrientVertex.getConnectionFieldName(direction, clsName, useVertexFieldsForEdgeLabels)
  }

  def outEdgePropertyName(edgeClassName: String)(implicit graph: OrientGraph): String = edgePropertyName(edgeClassName, Direction.OUT)
  def inEdgePropertyName(edgeClassName: String)(implicit graph: OrientGraph): String = edgePropertyName(edgeClassName, Direction.IN)
  def inoutEdgePropertyName(edgeClassName: String)(implicit graph: OrientGraph): String = edgePropertyName(edgeClassName, Direction.BOTH)
}
