package uk.co.pragmasoft.graphdb.orient.sampledao

import com.orientechnologies.orient.core.id.ORID
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory
import com.tinkerpop.blueprints.{Direction, TransactionalGraph, Vertex}
import uk.co.pragmasoft.graphdb.marshalling.{GraphMarshallingDSL, GraphMarshaller}
import uk.co.pragmasoft.graphdb.orient.{OrientDBBasicConversions, OrientDBSupport, OrientIndexNamingSupport}
import uk.co.pragmasoft.graphdb.validation.NoValidations


case class Fan(id: String, name: String, age: Int, fanOf: Seq[Artist])

case class Artist(id: String, name: String, styles: Seq[String], musicians: Seq[Musician])

case class Musician(id: String, name: String, instrument: String)


object FanMarshaller extends GraphMarshaller[Fan] with OrientDBBasicConversions {

  override def vertexClassName: String = "fan"

  private implicit val artistMarshaller = ArtistMarshaller

  private val AdoresLabel = "adores"
  private val NameAttribute = "name"
  private val AgeAttribute = "age"

  override def writeRelationships(data: Fan, vertex: Vertex)(implicit graphDb: TransactionalGraph): Unit = {
    vertex.removeEdges(AdoresLabel)

    data.fanOf foreach { artist =>
      vertex.addOutEdgeTo(artist, AdoresLabel)
    }
  }

  override def updateRelationships(data: Fan, vertex: Vertex)(implicit graphDb: TransactionalGraph) = {
    data.fanOf foreach { artist =>
      vertex.addOutEdgeTo(artist, AdoresLabel)
    }
  }


  override def propertiesForCreate(data: Fan) = attributesMap(data)

  override def propertiesForUpdate(data: Fan) = attributesMap(data)

  def attributesMap(data: Fan): Set[(String, Any)] = {
    Set(
      NameAttribute -> data.name,
      AgeAttribute -> data.age
    )
  }

  override def getModelObjectID(fan: Fan): String = fan.id


  override def read(vertex: Vertex)(implicit graphDb: TransactionalGraph) =
      Fan(
        id = vertex.getId.asInstanceOf[ORID],
        name = vertex.getProperty[String](NameAttribute),
        age = vertex.getProperty[Int](AgeAttribute),
        fanOf = vertex.outAdjacentsForLabel[Artist](AdoresLabel).toSeq
      )
}

object ArtistMarshaller extends GraphMarshaller[Artist] with OrientDBBasicConversions {
  override def vertexClassName: String = "artist"

  private implicit val musicianMarshaller = MusicianMarshaller

  private val PlaysInLabel = "plays-in"
  private val NameAttribute = "name"
  private val StylesAttribute = "styles"

  override def getModelObjectID(artist: Artist): String = artist.id

  override def read(vertex: Vertex)(implicit graphDb: TransactionalGraph): Artist =
    Artist(
      vertex.getId.asInstanceOf[ORID],
      vertex.getProperty[String](NameAttribute),
      vertex.getProperty(StylesAttribute).asInstanceOf[Seq[String]],
      vertex.inAdjacentsForLabel[Musician](PlaysInLabel).toSeq
    )

  override def propertiesForCreate(artist: Artist) = Set(
    NameAttribute -> artist.name,
    StylesAttribute -> artist.styles
  )

  // Cannot update name..
  override def propertiesForUpdate(artist: Artist) = Set(
    StylesAttribute -> artist.styles
  )

  override def writeRelationships(artist: Artist, vertex: Vertex)(implicit graphDb: TransactionalGraph) = {
    artist.musicians.foreach { musician =>
      vertex.addInEdgeFrom(musician, PlaysInLabel)
    }
  }
  
  override def updateRelationships(artist: Artist, vertex: Vertex)(implicit graphDb: TransactionalGraph) = {
    vertex.removeEdges(PlaysInLabel, Direction.IN )

    artist.musicians.foreach { musician =>
      vertex.addInEdgeFrom(musician, PlaysInLabel)
    }
  }
}

object MusicianMarshaller extends GraphMarshaller[Musician] with OrientDBBasicConversions {
  override def vertexClassName: String = "musician"

  override def getModelObjectID(obj: Musician): String = obj.id

  private val NameAttribute = "name"
  private val InstrumentAttribute = "instrument"

  override def propertiesForCreate(musician: Musician) =
    Set(
      NameAttribute -> musician.name,
      InstrumentAttribute -> musician.instrument
    )

  // Only changing instrument
  override def propertiesForUpdate(musician: Musician) =
    Set(
      InstrumentAttribute -> musician.instrument
    )

  override def read(vertex: Vertex)(implicit graphDb: TransactionalGraph) = 
    Musician(
      id = vertex.getId.asInstanceOf[ORID],
      name = vertex.getProperty[String](NameAttribute),
      instrument = vertex.getProperty[String](InstrumentAttribute)
    )
  
  override def updateProperties(musician: Musician, vertex: Vertex)(implicit graphDb: TransactionalGraph): Unit = {
    vertex.setProperty(InstrumentAttribute, musician.instrument)
  }
  
  override def updateRelationships(data: Musician, vertex: Vertex)(implicit graphDb: TransactionalGraph) = {}
  override def writeRelationships(data: Musician, vertex: Vertex)(implicit graphDb: TransactionalGraph) = {}
}



class FanDao(override val graphFactory: OrientGraphFactory) extends OrientDBSupport[Fan] with OrientIndexNamingSupport with OrientDBBasicConversions with NoValidations[Fan] {

  override def marshaller: GraphMarshaller[Fan] = FanMarshaller
}