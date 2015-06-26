package uk.co.pragmasoft.graphdb.orient

import com.orientechnologies.orient.core.id.ORID
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.{OrientGraph, OrientGraphFactory, OrientGraphNoTx}
import org.scalatest.{FlatSpec, Matchers}
import uk.co.pragmasoft.graphdb.orient.sampledao.{Musician, MusicianDao}
import uk.co.pragmasoft.graphdb.orient.sampledao.marshallers.{MusicianMarshaller, ArtistMarshaller, FanMarshaller}
import uk.co.pragmasoft.graphdb.orient.support.OrientDBMemoryTestSupport
import scala.collection.JavaConversions._

class OrientDBSupportSpec extends FlatSpec with Matchers with OrientDBMemoryTestSupport {

  override def initDB(db: OrientGraphNoTx): Unit = {
    db.createVertexType(FanMarshaller.vertexClassName)
    db.createVertexType(ArtistMarshaller.vertexClassName)
    db.createVertexType(MusicianMarshaller.vertexClassName)
  }

  protected def withTxDb[T](block: OrientGraph=> T)(implicit orientGraphFactory: OrientGraphFactory): T = {
    val graph = orientGraphFactory.getTx

    try {
      block(graph)
    } finally {
      graph.shutdown()
    }
  }

  protected def readVertex(id: Any)(implicit orientGraphFactory: OrientGraphFactory): Option[Vertex] = withTxDb { graph =>
    Option(graph.getVertex(id))
  }

  protected def writeVertex(id: Any, properties: Map[String, Any])(implicit orientGraphFactory: OrientGraphFactory): ORID = withTxDb { graph =>
    val created = graph.addVertex(id, properties)

    created.getIdentity
  }


  behavior of "Dao Object"

  it should "Write an object in the DB returning its ID" in withInMemoryOrientGraphDB { implicit graphFactory =>

    val musicianDao = new MusicianDao(graphFactory)

    val musician = musicianDao.create( Musician("", "Brian May", "guitar, piano, ukulele") )

    musician.id shouldNot be ('empty)

    readVertex(musician.id) should be ('defined)
  }

  it should "Read an object from the DB given its ID" in withInMemoryOrientGraphDB { implicit graphFactory =>

    writeVertex("1", Map( "name" -> "Brian May", "instrument" -> "guitar" ) )

    val musicianDao = new MusicianDao(graphFactory)

    val musicianMaybe = musicianDao.getById("1")

    musicianMaybe should be (Some(Musician("1", "Brian May", "guitar")))
  }
}
