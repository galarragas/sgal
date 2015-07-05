package uk.co.pragmasoft.graphdb.marshalling

import java.util
import java.util.Arrays.asList

import com.tinkerpop.blueprints.{Direction, Edge, TransactionalGraph, Vertex}
import org.mockito.Matchers.{any => anyArg, anyString, eq => argEq}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import uk.co.pragmasoft.graphdb.TestVertex


class GraphMarshallerSpec extends FlatSpec with Matchers with MockitoSugar with GraphMarshallingDSL {
  import TestVertexMarshaller._

  implicit val testVertexMarshaller = new TestVertexMarshaller

  val EmptyEdgeList = new util.ArrayList[Edge]()
  val EmptyVertexList = new util.ArrayList[Vertex]()

  behavior of "GraphMarshaller"

  it should "extract the ID field from the target type" in {
    TestVertex(1, "key", 10, None, Set.empty).getVertexId should be (1)
  }
  
  it should "write properties of a new object into a Vertex" in {
    implicit val graph = mock[TransactionalGraph]
    val vertex = mock[Vertex]
    
    TestVertex(-1, "key", 10, None, Set.empty) writePropertiesTo vertex

    verify(vertex).setProperty(Key, "key")
    verify(vertex).setProperty(Property, 10)
  }

  it should "add OUT edges for relationships from a new object into a Vertex" in {
    implicit val graph = mock[TransactionalGraph]
    val vertex = mock[Vertex]
    val otherVertex = mock[Vertex]
    
    when(graph.getVertex(Long.box(1))) thenReturn otherVertex
    
    TestVertex(-1,"key", 10, Some(TestVertex(1, "otherKey", 11, None, Set.empty)), Set.empty) writeRelationshipsTo vertex
    
    verify(vertex).addEdge(OutBoundRelationship, otherVertex)
  }

  it should "add IN edges for relationships from a new object into a Vertex" in {
    implicit val graph = mock[TransactionalGraph]
    val vertex = mock[Vertex]
    val otherVertex = mock[Vertex]

    when(graph.getVertex(Long.box(1))) thenReturn otherVertex

    TestVertex(-1, "key", 10, None, Set( TestVertex(1, "otherKey", 11, None, Set.empty) ) ) writeRelationshipsTo vertex

    verify(otherVertex).addEdge(InBoundRelationship, vertex)
  }

  it should "write properties of an updated object into a Vertex " in {
    implicit val graph = mock[TransactionalGraph]
    val vertex = mock[Vertex]

    TestVertex(-1, "key", 10, None, Set.empty) updatePropertiesTo vertex

    verify(vertex).setProperty(Property, 10)
    verifyNoMoreInteractions(vertex)
  }

  it should "update OUT edges for relationships from a new object into a Vertex" in {
    implicit val graph = mock[TransactionalGraph]
    val vertex = mock[Vertex]
    val otherVertex = mock[Vertex]

    // No previous edges connected
    when(vertex.getEdges(anyArg[Direction], anyString)) thenReturn EmptyEdgeList
    when(graph.getVertex(Long.box(1))) thenReturn otherVertex

    TestVertex(-1, "key", 10, Some(TestVertex(1, "otherKey", 11, None, Set.empty)), Set.empty) updateRelationshipsTo  vertex

    verify(vertex).addEdge(OutBoundRelationship, otherVertex)
  }

  it should "update IN edges for relationships from a new object into a Vertex" in {
    implicit val graph = mock[TransactionalGraph]
    val vertex = mock[Vertex]
    val otherVertex = mock[Vertex]

    // No previous edges connected
    when(vertex.getEdges(anyArg[Direction], anyString)) thenReturn EmptyEdgeList
    when(graph.getVertex(Long.box(1))) thenReturn otherVertex

    TestVertex(-1, "key", 10, None, Set( TestVertex(1, "otherKey", 11, None, Set.empty) ) ) updateRelationshipsTo vertex

    verify(otherVertex).addEdge(InBoundRelationship, vertex)
  }

  it should "fail to create IN edges if the associated vertex doesn't exist" in {
    implicit val graph = mock[TransactionalGraph]
    val vertex = mock[Vertex]
    val otherVertex = mock[Vertex]

    when(graph.getVertex(1)) thenReturn null

    intercept[IllegalArgumentException] {
      TestVertex(-1, "key", 10, None, Set(TestVertex(1, "otherKey", 11, None, Set.empty))) writeRelationshipsTo vertex
    }
  }

  it should "fail to create OUT edges if the associated vertex doesn't exist" in {
    implicit val graph = mock[TransactionalGraph]
    val vertex = mock[Vertex]
    val otherVertex = mock[Vertex]

    when(graph.getVertex("otherKey")) thenReturn null

    intercept[IllegalArgumentException] {
      TestVertex(-1, "key", 10, Some(TestVertex(-1, "otherKey", 11, None, Set.empty)), Set.empty) writeRelationshipsTo vertex
    }
  }

  it should "fail to update IN edges if the associated vertex doesn't exist" in {
    implicit val graph = mock[TransactionalGraph]
    val vertex = mock[Vertex]
    val otherVertex = mock[Vertex]

    when(vertex.getEdges(anyArg[Direction], anyString)) thenReturn EmptyEdgeList
    when(graph.getVertex(1)) thenReturn null

    intercept[IllegalArgumentException] {
      TestVertex(-1, "key", 10, None, Set(TestVertex(1, "otherKey", 11, None, Set.empty))) updateRelationshipsTo vertex
    }
  }

  it should "fail to update OUT edges if the associated vertex doesn't exist" in {
    implicit val graph = mock[TransactionalGraph]
    val vertex = mock[Vertex]
    val otherVertex = mock[Vertex]

    when(vertex.getEdges(anyArg[Direction], anyString)) thenReturn EmptyEdgeList
    when(graph.getVertex(1)) thenReturn null

    intercept[IllegalArgumentException] {
      TestVertex(-1, "key", 10, Some(TestVertex(1, "otherKey", 11, None, Set.empty)), Set.empty) writeRelationshipsTo vertex
    }
  }

  it should "read attribute from a Vertex" in {
    implicit val graph = mock[TransactionalGraph]
    val vertex = mock[Vertex]

    when(vertex.getId) thenReturn (Long.box(1l), Array.empty: _*)
    when(vertex.getProperty(Key)) thenReturn "key"
    when(vertex.getProperty(Property)) thenReturn 10

    when(vertex.getVertices(anyArg[Direction], anyString)) thenReturn EmptyVertexList
    
    vertex.as[TestVertex] should be(TestVertex(1, "key", 10, None, Set.empty))
  }


  it should "read from an option of a Vertex" in {
    implicit val graph = mock[TransactionalGraph]
    val vertex = mock[Vertex]
    val outVertex = mock[Vertex]
    val inVertex = mock[Vertex]

    when(vertex.getId) thenReturn (Long.box(1), Array.empty: _*)
    when(vertex.getProperty(Key)) thenReturn "key"
    when(vertex.getProperty(Property)) thenReturn 10
    when(vertex.getVertices(argEq(Direction.OUT), argEq(OutBoundRelationship))) thenReturn asList(outVertex)
    when(vertex.getVertices(argEq(Direction.IN), argEq(InBoundRelationship))) thenReturn asList(inVertex)

    when(outVertex.getId) thenReturn (Long.box(2), Array.empty: _*)
    when(outVertex.getProperty(Key)) thenReturn "outVertexKey"
    when(outVertex.getProperty(Property)) thenReturn 11
    when(outVertex.getVertices(anyArg[Direction], anyString)) thenReturn EmptyVertexList

    when(inVertex.getId) thenReturn (Long.box(3), Array.empty: _*)
    when(inVertex.getProperty(Key)) thenReturn "inVertexKey"
    when(inVertex.getProperty(Property)) thenReturn 12
    when(inVertex.getVertices(anyArg[Direction], anyString)) thenReturn EmptyVertexList

    vertex.as[TestVertex] should be(TestVertex(1, "key", 10, Some(TestVertex(2, "outVertexKey", 11, None, Set.empty)), Set(TestVertex(3, "inVertexKey", 12, None, Set.empty))))
  }

}
