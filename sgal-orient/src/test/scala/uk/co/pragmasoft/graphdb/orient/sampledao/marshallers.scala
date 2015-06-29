package uk.co.pragmasoft.graphdb.orient.sampledao

import com.orientechnologies.orient.core.id.ORID
import com.tinkerpop.blueprints.{Direction, TransactionalGraph, Vertex}
import uk.co.pragmasoft.graphdb.orient.{OrientDBBasicConversions, OrientGraphMarshaller}

object marshallers {

  implicit object MusicianMarshaller extends OrientGraphMarshaller[Musician] with OrientDBBasicConversions {
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

  implicit object BandMarshaller extends OrientGraphMarshaller[Band] with OrientDBBasicConversions {
    type IdType = String

    override def vertexClassName: String = "artist"

    val PlaysIn = "plays-in"
    val NameAttribute = "name"
    val StylesAttribute = "styles"

    override def getModelObjectID(artist: Band) = artist.id

    override def read(vertex: Vertex)(implicit graphDb: TransactionalGraph): Band =
      Band(
        vertex.getId.asInstanceOf[ORID],
        vertex.getProperty[String](NameAttribute),
        vertex.getProperty(StylesAttribute).asInstanceOf[Set[String]],
        vertex.inAdjacentsForLabel[Musician](PlaysIn).toSet
      )

    override def propertiesForCreate(artist: Band) = Set(
      NameAttribute -> artist.name,
      StylesAttribute -> artist.styles
    )

    // Cannot update name..
    override def propertiesForUpdate(artist: Band) = Set(
      StylesAttribute -> artist.styles
    )

    override def writeRelationships(artist: Band, vertex: Vertex)(implicit graphDb: TransactionalGraph) = {
      artist.musicians.foreach { musician =>
        vertex.addInEdgeFrom(musician, PlaysIn)
      }
    }

    override def updateRelationships(artist: Band, vertex: Vertex)(implicit graphDb: TransactionalGraph) = {
      vertex.removeEdges(PlaysIn, Direction.IN )

      artist.musicians.foreach { musician =>
        vertex <-- PlaysIn <-- musician
      }
    }
  }

  implicit object FanMarshaller extends OrientGraphMarshaller[Fan] with OrientDBBasicConversions {
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
        fanOf = vertex.outAdjacentsForLabel[Band](Adores).toSet
      )
  }
}
