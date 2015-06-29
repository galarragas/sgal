package uk.co.pragmasoft.graphdb.orient.sampledao

import com.orientechnologies.orient.core.id.ORID
import com.tinkerpop.blueprints.{Direction, TransactionalGraph, Vertex}
import uk.co.pragmasoft.graphdb.marshalling.GraphMarshaller
import uk.co.pragmasoft.graphdb.orient.OrientDBBasicConversions

object marshallers {

  implicit object MusicianMarshaller extends GraphMarshaller[Musician] with OrientDBBasicConversions {
    type IdType = String

    override def vertexClassName: String = "musician"

    override def getModelObjectID(obj: Musician) = obj.id

    val NameAttribute = "name"
    val InstrumentAttribute = "instrument"

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

  implicit object ArtistMarshaller extends GraphMarshaller[Artist] with OrientDBBasicConversions {
    type IdType = String

    override def vertexClassName: String = "artist"

    val PlaysIn = "plays-in"
    val NameAttribute = "name"
    val StylesAttribute = "styles"

    override def getModelObjectID(artist: Artist) = artist.id

    override def read(vertex: Vertex)(implicit graphDb: TransactionalGraph): Artist =
      Artist(
        vertex.getId.asInstanceOf[ORID],
        vertex.getProperty[String](NameAttribute),
        vertex.getProperty(StylesAttribute).asInstanceOf[Seq[String]],
        vertex.inAdjacentsForLabel[Musician](PlaysIn).toSeq
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
        vertex.addInEdgeFrom(musician, PlaysIn)
      }
    }

    override def updateRelationships(artist: Artist, vertex: Vertex)(implicit graphDb: TransactionalGraph) = {
      vertex.removeEdges(PlaysIn, Direction.IN )

      artist.musicians.foreach { musician =>
        vertex --> PlaysIn --> musician
      }
    }
  }

  implicit object FanMarshaller extends GraphMarshaller[Fan] with OrientDBBasicConversions {
    type IdType = String

    override def vertexClassName: String = "fan"

    val Adores = "adores"
    val NameAttribute = "name"
    val AgeAttribute = "age"

    override def writeRelationships(fanData: Fan, fanVertex: Vertex)(implicit graphDb: TransactionalGraph): Unit = {
      fanVertex.removeEdges(Adores)

      fanData.fanOf foreach { artist =>
        fanVertex --> Adores --> artist
      }
    }

    override def updateRelationships(fanData: Fan, fanVertex: Vertex)(implicit graphDb: TransactionalGraph) = {
      fanData.fanOf foreach { artist =>
        fanVertex --> Adores --> artist
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

    override def getModelObjectID(fan: Fan) = fan.id


    override def read(vertex: Vertex)(implicit graphDb: TransactionalGraph) =
      Fan(
        id = vertex.getId.asInstanceOf[ORID],
        name = vertex.getProperty[String](NameAttribute),
        age = vertex.getProperty[Int](AgeAttribute),
        fanOf = vertex.outAdjacentsForLabel[Artist](Adores).toSeq
      )
  }
}
