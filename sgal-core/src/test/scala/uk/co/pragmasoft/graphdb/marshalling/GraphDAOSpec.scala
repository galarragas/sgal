package uk.co.pragmasoft.graphdb.marshalling

import java.util

import com.tinkerpop.blueprints.{Edge, Direction, Vertex, TransactionalGraph}
import org.mockito.Mockito.{when, verify, verifyNoMoreInteractions}
import org.mockito.Matchers.{eq => argEq, any => anyArg, anyString}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import uk.co.pragmasoft.graphdb.validation.NoValidations
import TestVertexMarshaller._

class GraphDAOSpec extends FlatSpec with Matchers with MockitoSugar{
  val EmptyEdgeList = new util.ArrayList[Edge]()
  val EmptyVertexList = new util.ArrayList[Vertex]()

  behavior of "GrapDAO"

  it should "write a new entity in the db asking the marshaller to write properties and relationships" in {
    val graph = mock[TransactionalGraph]
    val marshaller = mock[GraphMarshaller[TestVertex]]
    val vertex = mock[Vertex]

    val dao = new TestVertexDao(graph, marshaller) with NoValidations[TestVertex]
    val entity = TestVertex("key", 1, None, Set.empty)

    when(marshaller.getModelObjectID(entity)).thenReturn("key".asInstanceOf[marshaller.IdType])
    when(graph.addVertex("key")) thenReturn vertex

    dao create entity

    verify(marshaller).writeProperties(entity, vertex)(graph)
    verify(marshaller).writeRelationships(entity, vertex)(graph)
  }


  it should "update an entity, collecting it from the DB asking the marshaller to update properties and relationships" in {
    val graph = mock[TransactionalGraph]
    val marshaller = mock[GraphMarshaller[TestVertex]]
    val vertex = mock[Vertex]

    val dao = new TestVertexDao(graph, marshaller) with NoValidations[TestVertex]
    val entity = TestVertex("key", 1, None, Set.empty)

    when(marshaller.getModelObjectID(entity)).thenReturn("key".asInstanceOf[marshaller.IdType])
    when(graph.getVertex("key")) thenReturn vertex

    dao update entity

    verify(marshaller).updateProperties(entity, vertex)(graph)
    verify(marshaller).updateRelationships(entity, vertex)(graph)
  }

  it should "fail to update an entity if not found in the DB" in {
    val graph = mock[TransactionalGraph]
    val marshaller = mock[GraphMarshaller[TestVertex]]

    val dao = new TestVertexDao(graph, marshaller) with NoValidations[TestVertex]
    val entity = TestVertex("key", 1, None, Set.empty)

    when(marshaller.getModelObjectID(entity)).thenReturn("key".asInstanceOf[marshaller.IdType])
    when(graph.getVertex("key")) thenReturn null

    intercept[IllegalArgumentException] {
      dao update entity
    }
  }

  it should "delete the associated vertex" in {
    val graph = mock[TransactionalGraph]
    val marshaller = mock[GraphMarshaller[TestVertex]]
    val vertex = mock[Vertex]

    val dao = new TestVertexDao(graph, marshaller) with NoValidations[TestVertex]
    val entity = TestVertex("key", 1, None, Set.empty)

    when(marshaller.getModelObjectID(entity)).thenReturn("key".asInstanceOf[marshaller.IdType])
    when(graph.getVertex("key")) thenReturn vertex

    (dao delete entity) should be (true)

    verify(graph) removeVertex vertex
  }

  it should "fail to delete if the associated vertex cannot be found" in {
    val graph = mock[TransactionalGraph]
    val marshaller = mock[GraphMarshaller[TestVertex]]

    val dao = new TestVertexDao(graph, marshaller) with NoValidations[TestVertex]
    val entity = TestVertex("key", 1, None, Set.empty)

    when(marshaller.getModelObjectID(entity)).thenReturn("key".asInstanceOf[marshaller.IdType])
    when(graph.getVertex("key")) thenReturn null

    (dao delete entity) should be (false)

  }

  it should "read a vertex by ID and ask the marshaller to read its content" in {
    val graph = mock[TransactionalGraph]
    val marshaller = mock[GraphMarshaller[TestVertex]]
    val vertex = mock[Vertex]

    when(graph.getVertex(argEq("key"))) thenReturn vertex

    val entity = TestVertex("key", 1, None, Set.empty)

    when(marshaller.readFrom(vertex)(graph)) thenReturn entity

    val dao = new TestVertexDao(graph, marshaller) with NoValidations[TestVertex]

    (dao getById "key") should be(Some(entity))
  }

  it should "return NONE if the vertex is not found" in {
    val graph = mock[TransactionalGraph]
    val marshaller = mock[GraphMarshaller[TestVertex]]

    when(graph.getVertex(argEq("key"))) thenReturn null

    val dao = new TestVertexDao(graph, marshaller) with NoValidations[TestVertex]

    (dao getById "key") should be(None)
  }
}