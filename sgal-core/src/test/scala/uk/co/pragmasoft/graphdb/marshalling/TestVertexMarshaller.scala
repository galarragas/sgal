package uk.co.pragmasoft.graphdb.marshalling

import com.tinkerpop.blueprints.{TransactionalGraph, Vertex}
import uk.co.pragmasoft.graphdb.TestVertex

class TestVertexMarshaller extends GraphMarshaller[TestVertex] {
  import TestVertexMarshaller._

  override type IdType = Long

  override def getModelObjectID(obj: TestVertex) = obj.id

  override protected def propertiesForCreate(data: TestVertex) =
    Set(
      Key -> data.key,
      Property -> data.property
    )

  override protected def propertiesForUpdate(data: TestVertex) = Set( Property -> data.property )


  override def writeRelationships(data: TestVertex, vertex: Vertex)(implicit graphDb: TransactionalGraph) = {
    implicit val targetRelationshipMarshaller = this

    data.relationship1 foreach { target =>
      vertex --> OutBoundRelationship --> target
    }

    data.relationship2 foreach { target =>
      vertex <-- InBoundRelationship <-- target
    }
  }

  override def updateRelationships(data: TestVertex, vertex: Vertex)(implicit graphDb: TransactionalGraph) = {
    // Dumb implementation, removing all old edges and re-creating them
    vertex.removeEdges(OutBoundRelationship)
    vertex.removeEdges(InBoundRelationship)

    writeRelationships(data, vertex)
  }


  override def readFrom(vertex: Vertex)(implicit graphDb: TransactionalGraph): TestVertex = {
    implicit val targetRelationshipMarshaller = this

    TestVertex(
      vertex.getId.asInstanceOf[Long],
      vertex.getProperty(Key),
      vertex.getProperty(Property),
      vertex.outAdjacentsForLabel(OutBoundRelationship).headOption,
      vertex.inAdjacentsForLabel(InBoundRelationship).toSet
    )
  }
}

object TestVertexMarshaller {
  val OutBoundRelationship: String = "out-bound"
  val InBoundRelationship: String = "in-bound"
  val Key: String = "key"
  val Property: String = "prop2"
}