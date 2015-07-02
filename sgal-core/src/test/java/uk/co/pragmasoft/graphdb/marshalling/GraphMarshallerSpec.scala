package uk.co.pragmasoft.graphdb.marshalling

import com.tinkerpop.blueprints.{TransactionalGraph, Vertex}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import uk.co.pragmasoft.graphdb.validation.NoValidations

class GraphMarshallerSpec extends FlatSpec with Matchers with MockitoSugar {

  val OutBoundRelationship: String = "out-bound"
  val InBoundRelationship: String = "in-bound"
  val Key: String = "key"
  val Property: String = "prop2"

  case class TestEdge(key: String, property: Int, relationship1: Option[TestEdge], relationship2: Set[TestEdge])


  implicit val testEdgeMarshaller = new GraphMarshaller[TestEdge] with NoValidations[TestEdge] {

    override type IdType = String
    
    override def getModelObjectID(obj: TestEdge): IdType = obj.key
    
    override protected def propertiesForCreate(data: TestEdge) =
      Set(
        Key -> data.key,
        Property -> data.property
      )
    
    override protected def propertiesForUpdate(data: TestEdge) = Set( Property -> data.property )

    
    override def writeRelationships(data: TestEdge, vertex: Vertex)(implicit graphDb: TransactionalGraph) = {
      implicit val targetRelationshipMarshaller = this

      data.relationship1 foreach { target =>
        vertex --> OutBoundRelationship --> target
      }
      
      data.relationship2 foreach { target =>
        vertex <-- InBoundRelationship <-- target
      }
    }

    override def updateRelationships(data: TestEdge, vertex: Vertex)(implicit graphDb: TransactionalGraph) = {
      // Dumb implementation, removing all old edges and re-creating them
      vertex.removeEdges(OutBoundRelationship)
      vertex.removeEdges(InBoundRelationship)

      writeRelationships(data, vertex)
    }


    override def read(vertex: Vertex)(implicit graphDb: TransactionalGraph): TestEdge = {
      implicit val targetRelationshipMarshaller = this

      TestEdge(
        vertex.getProperty(Key),
        vertex.getProperty(Property),
        vertex.outAdjacentsForLabel(OutBoundRelationship).headOption,
        vertex.inAdjacentsForLabel(InBoundRelationship).toSet
      )
    }
  }
  

  import GraphMarshallingDSL._
  
  behavior of "GraphMarshaller"

  it should "extract the ID field from the target type" in {
    TestEdge("key", 10, None, Set.empty).getVertexId should be ("key")
  }
  
  it should "write properties of a new object into a Vertex" in {
    implicit val graph = mock[TransactionalGraph]
    val vertex = mock[Vertex]
    
    TestEdge("key", 10, None, Set.empty) writePropertiesTo vertex

    verify(vertex).setProperty(Key, "key")
    verify(vertex).setProperty(Property, 10)
  }

  it should "add output edges for relationships from a new object into a Vertex" in {
    implicit val graph = mock[TransactionalGraph]
    val vertex = mock[Vertex]
    val otherVertex = mock[Vertex]
    
    when(graph.getVertex("otherKey")) thenReturn otherVertex
    
    TestEdge("key", 10, Some(TestEdge("otherKey", 11, None, Set.empty)), Set.empty) writeRelationshipsTo vertex
    
    verify(vertex).addEdge(OutBoundRelationship, otherVertex)
  }

  it should "add input edges for relationships from a new object into a Vertex" in {
    implicit val graph = mock[TransactionalGraph]
    val vertex = mock[Vertex]
    val otherVertex = mock[Vertex]

    when(graph.getVertex("otherKey")) thenReturn otherVertex

    TestEdge("key", 10, None, Set( TestEdge("otherKey", 11, None, Set.empty) ) ) writeRelationshipsTo vertex

    verify(otherVertex).addEdge(InBoundRelationship, vertex)
  }

  it should "write propertyes of an updated object into a Vertex " in {
    implicit val graph = mock[TransactionalGraph]
    val vertex = mock[Vertex]

    TestEdge("key", 10, None, Set.empty) updatePropertiesTo vertex

    verify(vertex).setProperty(Property, 10)
    verifyNoMoreInteractions(vertex)
  }

  

}
