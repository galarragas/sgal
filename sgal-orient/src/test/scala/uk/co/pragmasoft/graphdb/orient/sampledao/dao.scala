package uk.co.pragmasoft.graphdb.orient.sampledao

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory
import com.tinkerpop.gremlin.scala._
import uk.co.pragmasoft.graphdb.validation.NoValidations
import uk.co.pragmasoft.graphdb.orient.sampledao.marshallers.{BandMarshaller, FanMarshaller, MusicianMarshaller}
import uk.co.pragmasoft.graphdb.orient.{OrientDBBasicConversions, OrientDbDAO, OrientIndexNamingSupport}

class FanDao(override val graphFactory: OrientGraphFactory) extends OrientDbDAO[Fan] with OrientIndexNamingSupport with OrientDBBasicConversions with NoValidations[Fan] {

  override def marshaller = FanMarshaller

  def findBySupportedArtist(band: Band): Iterable[Fan] = withGraphDb { implicit db =>
    vertexFor(band).fold(Stream.empty[Fan]) { vertex =>
      vertex.in(marshaller.Adores)
        .map( _.as[Fan] )
        .toStream()
    }
  }

}

class BandDao(override val graphFactory: OrientGraphFactory) extends OrientDbDAO[Band] with OrientIndexNamingSupport with OrientDBBasicConversions with NoValidations[Band] {

  override def marshaller = BandMarshaller
  
  def findByMusician(musician: Musician): Iterable[Band] = withGraphDb { implicit db =>
    vertexFor(musician).fold(Stream.empty[Band]) { vertex =>
      vertex.out(marshaller.PlaysIn)
        .map( _.as[Band] )
        .toStream()
    }
  }

}

class MusicianDao(override val graphFactory: OrientGraphFactory) extends OrientDbDAO[Musician] with OrientIndexNamingSupport with OrientDBBasicConversions with NoValidations[Musician] {

  override def marshaller = MusicianMarshaller


}

