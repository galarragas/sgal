package uk.co.pragmasoft.graphdb.orient.support

import com.orientechnologies.orient.core.id.ORID
import com.orientechnologies.orient.core.metadata.schema.{OClass, OType}
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.{OrientGraph, OrientGraphFactory, OrientGraphNoTx}
import uk.co.pragmasoft.graphdb.orient.sampledao.marshallers.{BandMarshaller, FanMarshaller, MusicianMarshaller}

/**
 * Created by stefano on 29/06/15.
 */
trait MusicFixtureDBSupport {
  def initDB(db: OrientGraphNoTx): Unit = {
    val fan = db.createVertexType(FanMarshaller.vertexClassName)
    // Fan has a Lucene index on the attribute name
    fan.createProperty(FanMarshaller.NameAttribute, OType.STRING).createIndex(OClass.INDEX_TYPE.NOTUNIQUE)
    fan.createProperty(FanMarshaller.AgeAttribute, OType.INTEGER).createIndex(OClass.INDEX_TYPE.NOTUNIQUE_HASH_INDEX)

    val band = db.createVertexType(BandMarshaller.vertexClassName)
    band.createProperty(BandMarshaller.NameAttribute, OType.STRING).createIndex(OClass.INDEX_TYPE.FULLTEXT)
    band.createProperty(BandMarshaller.StylesAttribute, OType.EMBEDDEDSET)

    val musician = db.createVertexType(MusicianMarshaller.vertexClassName)
    musician.createProperty(MusicianMarshaller.NameAttribute, OType.STRING).createIndex(OClass.INDEX_TYPE.FULLTEXT)
    musician.createProperty(MusicianMarshaller.InstrumentAttribute, OType.STRING).createIndex(OClass.INDEX_TYPE.FULLTEXT)


    db.createEdgeType(BandMarshaller.PlaysIn)
    db.createEdgeType(FanMarshaller.Adores)
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
    val created = graph.addVertex(id, Array(): _*)
    properties.foreach { case (key, value) =>
      created.setProperty(key, value)
    }

    created.getIdentity
  }
}
