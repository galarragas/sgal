package uk.co.pragmasoft.graphdb.titan

import org.scalatest.{Matchers, FlatSpec}
import uk.co.pragmasoft.graphdb.TestVertex


class TitanGraphSpec extends FlatSpec with Matchers with InMemoryTitanDaoSupport {

  behavior of "Titan Graph DAO"

  it should "Save and retrieve an object in Titan" in withTitanDao { implicit graph =>
    val dao = new TitanTestVertexDao(graph)

    val newVertex = dao.create( TestVertex(-1, "key", 10, None, Set.empty ) )

    dao.getById(newVertex.id) should be( Some(newVertex) )
  }

  it should "Update and retrieve updated object in Titan" in withTitanDao { implicit graph =>
    val dao = new TitanTestVertexDao(graph)

    val newVertex = dao.create( TestVertex(-1, "key", 10, None, Set.empty ) )
    val updated = dao.update( newVertex.copy( key = "new key") )

    dao.getById(newVertex.id) should be( Some(updated) )
  }

  it should "Save and retrieve an object with relationships in Titan" in withTitanDao { implicit graph =>
    val dao = new TitanTestVertexDao(graph)

    val relationshipObj = dao.create( TestVertex(-1, "other", 10, None, Set.empty ) )
    val newVertex = dao.create( TestVertex(-1, "key", 10, Some(relationshipObj), Set.empty ) )

    dao.getById(newVertex.id) should be( Some(newVertex) )
  }

  it should "Update and retrieve an object with relationships in Titan" in withTitanDao { implicit graph =>
    val dao = new TitanTestVertexDao(graph)

    val relationshipObj = dao.create( TestVertex(-1, "other", 10, None, Set.empty ) )
    val secondRelationshipObj = dao.create( TestVertex(-1, "another", 10, None, Set.empty ) )
    val newVertex = dao.create( TestVertex(-1, "key", 10, None, Set(relationshipObj) ) )

    val updated = dao.update(
      newVertex.copy(
        key = "new key",
        relationship2 = (newVertex.relationship2 + secondRelationshipObj)
      )
    )

    dao.getById(newVertex.id) should be( Some(updated) )
  }

  it should "delete an object" in withTitanDao { implicit graph =>
    val dao = new TitanTestVertexDao(graph)

    val newVertex = dao.create( TestVertex(-1, "key", 10, None, Set.empty ) )

    dao.delete(newVertex) should be(true)

    dao.getById(newVertex.id) should be (None)
  }

  it should "navigate relationships" in withTitanDao { implicit graph =>
    val dao = new TitanTestVertexDao(graph)

    val relationshipObj = dao.create( TestVertex(-1, "other", 10, None, Set.empty ) )

    val newVertex1 = dao.create( TestVertex(-1, "key", 10, None, Set(relationshipObj) ) )
    val newVertex2 = dao.create( TestVertex(-1, "another", 10, None, Set(relationshipObj) ) )


    dao.findByRelationship2(relationshipObj).toSet should be( Set(newVertex1, newVertex2) )
  }

}
