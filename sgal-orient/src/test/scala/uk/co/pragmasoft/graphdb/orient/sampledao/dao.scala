package uk.co.pragmasoft.graphdb.orient.sampledao

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory
import com.tinkerpop.gremlin.scala._
import uk.co.pragmasoft.graphdb.orient.sampledao.marshallers.{ArtistMarshaller, FanMarshaller, MusicianMarshaller}
import uk.co.pragmasoft.graphdb.orient.{OrientDBBasicConversions, OrientDBSupport, OrientIndexNamingSupport}
import uk.co.pragmasoft.graphdb.validation.NoValidations

class FanDao(override val graphFactory: OrientGraphFactory) extends OrientDBSupport[Fan] with OrientIndexNamingSupport with OrientDBBasicConversions with NoValidations[Fan] {

  override def marshaller = FanMarshaller

  def findBySupportedArtist(artist: Artist): Iterable[Fan] = withGraphDb { implicit db =>

    vertexFor(artist)
      .inE(marshaller.AdoresLabel)
      .outV
      .map { vertex => vertex.as[Fan] }
      .toStream()
  }

}

class ArtistDao(override val graphFactory: OrientGraphFactory) extends OrientDBSupport[Artist] with OrientIndexNamingSupport with OrientDBBasicConversions with NoValidations[Artist] {

  override def marshaller = ArtistMarshaller


}

class MusicianDao(override val graphFactory: OrientGraphFactory) extends OrientDBSupport[Musician] with OrientIndexNamingSupport with OrientDBBasicConversions with NoValidations[Musician] {

  override def marshaller = MusicianMarshaller


}

