package uk.co.pragmasoft.graphdb

import com.tinkerpop.blueprints.{Edge, TransactionalGraph, Vertex}
import org.mockito.Matchers.{any => anyArg, anyString, eq => argEq}
import org.mockito.Mockito.{verify, when}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import uk.co.pragmasoft.graphdb.marshalling.GraphMarshaller
import uk.co.pragmasoft.graphdb.validation.{GraphDAOValidations, NoValidations, ValiDataValidations}
import uk.co.pragmasoft.validate.{BaseValidations, TypeValidator}

import scala.collection.mutable

class GraphDAOSpec extends FlatSpec with Matchers with MockitoSugar{
  val EmptyEdgeList = new java.util.ArrayList[Edge]()
  val EmptyVertexList = new java.util.ArrayList[Vertex]()

  behavior of "GrapDAO"

  it should "write a new entity in the db asking the marshaller to write properties and relationships" in {
    val graph = mock[TransactionalGraph]
    val marshaller = mock[GraphMarshaller[TestVertex]]
    val vertex = mock[Vertex]

    val dao = new DITestVertexDao(graph, marshaller) with NoValidations[TestVertex]
    val entity = TestVertex(-1, "key", 1, None, Set.empty)

    when(marshaller.getModelObjectID(entity)).thenReturn((-1l).asInstanceOf[marshaller.IdType])
    when(graph.addVertex(Long.box(-1))) thenReturn vertex

    dao create entity

    verify(marshaller).writeProperties(entity, vertex)(graph)
    verify(marshaller).writeRelationships(entity, vertex)(graph)
  }


  it should "update an entity, collecting it from the DB asking the marshaller to update properties and relationships" in {
    val graph = mock[TransactionalGraph]
    val marshaller = mock[GraphMarshaller[TestVertex]]
    val vertex = mock[Vertex]

    val dao = new DITestVertexDao(graph, marshaller) with NoValidations[TestVertex]
    val entity = TestVertex(1, "key", 1, None, Set.empty)

    when(marshaller.getModelObjectID(entity)).thenReturn( (1).asInstanceOf[marshaller.IdType])
    when(graph.getVertex(1)) thenReturn vertex

    dao update entity

    verify(marshaller).updateProperties(entity, vertex)(graph)
    verify(marshaller).updateRelationships(entity, vertex)(graph)
  }

  it should "fail to update an entity if not found in the DB" in {
    val graph = mock[TransactionalGraph]
    val marshaller = mock[GraphMarshaller[TestVertex]]

    val dao = new DITestVertexDao(graph, marshaller) with NoValidations[TestVertex]
    val entity = TestVertex(1, "key", 1, None, Set.empty)

    when(marshaller.getModelObjectID(entity)).thenReturn(1.asInstanceOf[marshaller.IdType])
    when(graph.getVertex(1)) thenReturn null

    intercept[IllegalArgumentException] {
      dao update entity
    }
  }

  it should "delete the associated vertex" in {
    val graph = mock[TransactionalGraph]
    val marshaller = mock[GraphMarshaller[TestVertex]]
    val vertex = mock[Vertex]

    val dao = new DITestVertexDao(graph, marshaller) with NoValidations[TestVertex]
    val entity = TestVertex(1, "key", 1, None, Set.empty)

    when(marshaller.getModelObjectID(entity)).thenReturn(1.asInstanceOf[marshaller.IdType])
    when(graph.getVertex(1)) thenReturn vertex

    (dao delete entity) should be (true)

    verify(graph) removeVertex vertex
  }

  it should "fail to delete if the associated vertex cannot be found" in {
    val graph = mock[TransactionalGraph]
    val marshaller = mock[GraphMarshaller[TestVertex]]

    val dao = new DITestVertexDao(graph, marshaller) with NoValidations[TestVertex]
    val entity = TestVertex(1, "key", 1, None, Set.empty)

    when(marshaller.getModelObjectID(entity)).thenReturn(1.asInstanceOf[marshaller.IdType])
    when(graph.getVertex(1)) thenReturn null

    (dao delete entity) should be (false)

  }

  it should "read a vertex by ID and ask the marshaller to read its content" in {
    val graph = mock[TransactionalGraph]
    val marshaller = mock[GraphMarshaller[TestVertex]]
    val vertex = mock[Vertex]

    when(graph.getVertex(argEq(1))) thenReturn vertex

    val entity = TestVertex(1, "key", 1, None, Set.empty)

    when(marshaller.readFrom(vertex)(graph)) thenReturn entity

    val dao = new DITestVertexDao(graph, marshaller) with NoValidations[TestVertex]

    (dao getById 1) should be(Some(entity))
  }

  it should "return NONE if the vertex is not found" in {
    val graph = mock[TransactionalGraph]
    val marshaller = mock[GraphMarshaller[TestVertex]]

    when(graph.getVertex(argEq("key"))) thenReturn null

    val dao = new DITestVertexDao(graph, marshaller) with NoValidations[TestVertex]

    (dao getById "key") should be(None)
  }


  it should "validate on creation" in {
    val graph = mock[TransactionalGraph]
    val marshaller = mock[GraphMarshaller[TestVertex]]

    val dao = new DITestVertexDao(graph, marshaller) with RecordingValidations[TestVertex]

    val entity = TestVertex(-1, "key", 1, None, Set.empty)

    dao create entity

    dao.validatedForCreate.toList should be(List(entity))
  }

  it should "validate on update" in {
    val graph = mock[TransactionalGraph]
    val marshaller = mock[GraphMarshaller[TestVertex]]
    val vertex = mock[Vertex]

    val dao = new DITestVertexDao(graph, marshaller) with RecordingValidations[TestVertex]

    val entity = TestVertex(1, "key", 1, None, Set.empty)

    when(graph.getVertex(anyString)) thenReturn vertex

    dao update entity

    dao.validatedForUpdate.toList should be(List(entity))
  }

  it should "fail if validate on creation fails" in {
    val graph = mock[TransactionalGraph]
    val marshaller = mock[GraphMarshaller[TestVertex]]

    val dao = new DITestVertexDao(graph, marshaller) with FailingValidations[TestVertex]

    intercept[IllegalArgumentException] {
      dao create TestVertex(1, "key", 1, None, Set.empty)
    }
  }

  it should "fail if validate on update fails" in {
    val graph = mock[TransactionalGraph]
    val marshaller = mock[GraphMarshaller[TestVertex]]
    val vertex = mock[Vertex]

    val dao = new DITestVertexDao(graph, marshaller) with FailingValidations[TestVertex]

    when(graph.getVertex(anyString)) thenReturn vertex

    intercept[IllegalArgumentException] {
      dao update TestVertex(1, "key", 1, None, Set.empty)
    }
  }

  trait RecordingValidations[T] extends GraphDAOValidations[T] {

    val validatedForCreate: mutable.Buffer[T] = mutable.Buffer.empty[T]
    val validatedForUpdate: mutable.Buffer[T] = mutable.Buffer.empty[T]

    override def validateUpdate(updatedInstance: T): T = {
      validatedForUpdate append updatedInstance

      updatedInstance
    }

    override def validateNew(newInstance: T): T = {
      validatedForCreate append newInstance

      newInstance
    }
  }

  trait FailingValidations[T] extends GraphDAOValidations[T] {
   override def validateUpdate(updatedInstance: T): T = throw new IllegalArgumentException("expected exception for test purposes")

    override def validateNew(newInstance: T): T = throw new IllegalArgumentException("expected exception for test purposes")
  }

  trait TestVertexValiDataValidations extends ValiDataValidations[TestVertex] {

    override val newInstanceValidator = new TypeValidator[TestVertex] with BaseValidations {
      override def validations =
        ( "Key" definedBy { _.key } must beNotEmpty ) and ( "Property" definedBy { _.property} must bePositive[Int] )
    }

    override val updatedInstanceValidator = new TypeValidator[TestVertex] with BaseValidations {
      override def validations =
        "Property" definedBy { _.property} must bePositive[Int]
    }
  }
}
