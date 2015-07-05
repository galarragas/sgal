package uk.co.pragmasoft.graphdb.titan

import org.scalatest.{Matchers, FlatSpec}
import uk.co.pragmasoft.graphdb.TestVertex


class TitanGraphSpec extends FlatSpec with Matchers with InMemoryTitanDao {

  behavior of "Titan Graph DAO"

  it should "Save and retrieve an object in Titan" ignore withTitanDao { implicit graph =>
    val dao = new TitanTestVertexDao(graph)

    val newVertex = dao.create( TestVertex(-1, "key", 10, None, Set.empty ) )

    dao.getById(newVertex.key) should be(newVertex)

  }



}
