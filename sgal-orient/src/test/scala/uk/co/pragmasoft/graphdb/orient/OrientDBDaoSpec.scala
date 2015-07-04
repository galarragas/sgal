package uk.co.pragmasoft.graphdb.orient

import com.tinkerpop.gremlin.scala._
import org.scalatest.{FlatSpec, Matchers}
import uk.co.pragmasoft.graphdb.orient.sampledao.marshallers.BandMarshaller
import uk.co.pragmasoft.graphdb.orient.sampledao.{Band, BandDao, Musician, MusicianDao}
import uk.co.pragmasoft.graphdb.orient.support.{MusicFixtureDBSupport, OrientDBMemoryTestSupport}



class OrientDBDaoSpec extends FlatSpec with Matchers with OrientDBMemoryTestSupport with MusicFixtureDBSupport {

  behavior of "Orient DB Dao Object"

  it should "Write an object in the DB returning its ID" in withInMemoryOrientGraphDB { implicit graphFactory =>

    val musicianDao = new MusicianDao(graphFactory)

    val musician = musicianDao.create( Musician("", "Brian May", "guitar, piano, ukulele") )

    musician.id shouldNot be ('empty)

    readVertex(musician.id) should be ('defined)
  }

  it should "Read an object from the DB given its ID" in withInMemoryOrientGraphDB { implicit graphFactory =>

    // Write the object using the right class if you want to retrieve it
    val vertexId = writeVertex("class:musician", Map( "name" -> "Brian May", "instrument" -> "guitar" ) )

    val musicianDao = new MusicianDao(graphFactory)

    val musicianMaybe = musicianDao.getById(vertexId)

    musicianMaybe should be (Some(Musician(vertexId.toString, "Brian May", "guitar")))
  }

  it should "update updatable properties ignoring the others" in withInMemoryOrientGraphDB { implicit graphFactory =>

    val musicianDao = new MusicianDao(graphFactory)

    val created = musicianDao.create( Musician("", "Brian May", "guitar, piano, ukulele") )

    val updated = musicianDao.update( created.copy( name = "This should be ignored", instrument = "red special" ) )

    updated should be ( created.copy(instrument = "red special" ) )
  }

  it should "update relationships" in withInMemoryOrientGraphDB { implicit graphFactory =>
    val bandDao = new BandDao(graphFactory)
    val musicianDao = new MusicianDao(graphFactory)

    val brianMay = musicianDao.create( Musician("", "Brian May", "guitar") )
    val rogerTaylor = musicianDao.create( Musician("", "Roger Taylor", "drums") )
    val freddyMercury = musicianDao.create( Musician("", "Freddy Mercury", "vocals") )
    val johnDeacon = musicianDao.create( Musician("", "John Deacon", "bass") )

    val queen = bandDao.create(
      Band("", "Queen", Set("rock", "pop", "prog", "dance"), Set(brianMay, rogerTaylor, freddyMercury, johnDeacon))
    )

    withTxDb { graph =>
      graph.getVertex(queen.id).in(BandMarshaller.PlaysIn).toSet.map(_.id.toString) should be(queen.musicians.map(_.id))
    }

  }

  it should "fail trying to create relationships to objects not in DB" in withInMemoryOrientGraphDB { implicit graphFactory =>
    val bandDao = new BandDao(graphFactory)
    val musicianDao = new MusicianDao(graphFactory)

    val brianMay = musicianDao.create( Musician("", "Brian May", "guitar") )
    val rogerTaylor = musicianDao.create( Musician("", "Roger Taylor", "drums") )
    val freddyMercury = musicianDao.create( Musician("", "Freddy Mercury", "vocals") )

    intercept[IllegalArgumentException] {
      bandDao.create(
        Band("", "Queen", Set("rock", "pop", "prog", "dance"), Set(brianMay, rogerTaylor, freddyMercury, Musician("", "John Deacon", "bass")))
      )
    }

    val johnDeacon = musicianDao.create( Musician("", "John Deacon", "bass") )

    bandDao.create(
      Band("", "Queen", Set("rock", "pop", "prog", "dance"), Set(brianMay, rogerTaylor, freddyMercury, johnDeacon))
    )
  }

  it should "query using indexes" in withInMemoryOrientGraphDB { implicit graphFactory =>

    val bandDao = new BandDao(graphFactory)

    val ratm = bandDao.create( Band("", "Rage Against The Machine", Set("rock", "metal", "rap", "crossover"), Set.empty) )

    bandDao.findByPartialName("Against").toList should be (List(ratm))

  }

  it should "return NONE instead of trying to read a wrong object if asked to retrieve an Vertex of a different class" in withInMemoryOrientGraphDB { implicit graphFactory =>
    val bandDao = new BandDao(graphFactory)
    val musicianDao = new MusicianDao(graphFactory)

    val brianMay = musicianDao.create( Musician("", "Brian May", "guitar") )
    val ratm = bandDao.create( Band("", "Rage Against The Machine", Set("rock", "metal", "rap", "crossover"), Set.empty) )

    bandDao.getById(brianMay.id) should be(None)

  }

}
