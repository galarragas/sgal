package uk.co.pragmasoft.graphdb.orient.sampledao

import com.tinkerpop.blueprints.impls.orient.{OrientGraph, OrientGraphFactory}
import com.tinkerpop.gremlin.scala._
import uk.co.pragmasoft.graphdb.orient.sampledao.marshallers.{BandMarshaller, FanMarshaller, MusicianMarshaller}
import uk.co.pragmasoft.graphdb.orient.{OrientDBBasicConversions, OrientDbDAO, OrientIndexNamingSupport}
import uk.co.pragmasoft.graphdb.validation.NoValidations

import scala.collection.JavaConversions._

class FanDao(override val graphFactory: OrientGraphFactory) extends OrientDbDAO[Fan] with OrientIndexNamingSupport with OrientDBBasicConversions with NoValidations[Fan] {
  def findByName(fullName: String): Iterable[Fan] = withGraphDb { implicit db =>
    queryForEntityClass.has(marshaller.NameAttribute, fullName).vertices.map( _.as[Fan] )
  }


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
        .map { _.as[Band] }
        .toStream()
    }
  }


  def findByPartialName(partialName: String): Iterable[Band] = withGraphDb { implicit db =>
    implicit val orientDb = db.asInstanceOf[OrientGraph]

    // A LUCENE index is defined on the Band Name
    findByIndexedProperty(marshaller.NameAttribute, partialName).toIterable
  }
}

class MusicianDao(override val graphFactory: OrientGraphFactory) extends OrientDbDAO[Musician] with OrientIndexNamingSupport with OrientDBBasicConversions with NoValidations[Musician] {

  override def marshaller = MusicianMarshaller


}

