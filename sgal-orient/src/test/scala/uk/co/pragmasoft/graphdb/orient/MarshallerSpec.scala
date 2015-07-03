package uk.co.pragmasoft.graphdb.orient

import org.scalatest.{Matchers, FlatSpec}
import uk.co.pragmasoft.graphdb.orient.sampledao.{Band, Musician}
import uk.co.pragmasoft.graphdb.orient.sampledao.marshallers.{BandMarshaller, MusicianMarshaller}
import uk.co.pragmasoft.graphdb.orient.support.{MusicFixtureDBSupport, OrientDBMemoryTestSupport}
import com.tinkerpop.gremlin.scala._

class MarshallerSpec extends FlatSpec with Matchers with OrientDBMemoryTestSupport with MusicFixtureDBSupport {

  behavior of "Marshaller"

  it should "Read attributes from a vertex" in withInMemoryOrientGraphDB { implicit graphFactory =>

    val id = writeVertex("", Map( MusicianMarshaller.NameAttribute -> "Brian May", MusicianMarshaller.InstrumentAttribute -> "guitar" ) )

    withinTx { implicit graph =>
      MusicianMarshaller.readFrom(readVertex(id).get) should be(Musician(id.toString, "Brian May", "guitar"))
    }
  }

  it should "Write properties to a vertex" in withInMemoryOrientGraphDB { implicit graphFactory =>

    val id = writeVertex("", Map.empty )

    withinTx { implicit graph =>
      BandMarshaller.writeProperties(Band("", "Queen", Set("pop", "rock"), Set.empty), readVertex(id).get)
    }

    val populatedVertex = readVertex(id).get
    populatedVertex.getProperty[String](BandMarshaller.NameAttribute) should be ("Queen")
    populatedVertex.getProperty[Set[String]](BandMarshaller.StylesAttribute) should be ( Set("pop", "rock") )
  }


  it should "write relationships" in withInMemoryOrientGraphDB { implicit graphFactory =>
    val brianMayId: String = writeVertex("", Map.empty).toString
    val queenId: String = writeVertex("", Map.empty ).toString

    val queen = Band(queenId, "queen", Set.empty, Set( Musician(brianMayId, "Brian May", "guitar") ))

    withinTx { implicit graph =>
      BandMarshaller.writeRelationships(queen, readVertex(queenId).get)
    }

    val queenVertex = readVertex(queenId).get

    withinTx { implicit graph =>
      queenVertex.in( BandMarshaller.PlaysIn ).toSet.map( _.id.toString ) should be ( Set(brianMayId) )
    }
  }

  it should "allow to access vertex class name and vertex spec name of an entity, given its marshaller" in {
    import OrientGraphDSL._

    Musician("id", "name", "instrument").vertexClassName should be(MusicianMarshaller.vertexClassName)
    Musician("id", "name", "instrument").vertexClassSpec should be(MusicianMarshaller.vertexClassSpec)
  }

}
