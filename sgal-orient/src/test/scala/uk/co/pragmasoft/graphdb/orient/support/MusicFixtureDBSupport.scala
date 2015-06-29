package uk.co.pragmasoft.graphdb.orient.support

import com.orientechnologies.orient.core.id.ORID
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.{OrientGraph, OrientGraphFactory, OrientGraphNoTx}
import uk.co.pragmasoft.graphdb.orient.sampledao.marshallers.{BandMarshaller, FanMarshaller, MusicianMarshaller}

/**
 * Created by stefano on 29/06/15.
 */
trait MusicFixtureDBSupport {
  def initDB(db: OrientGraphNoTx): Unit = {
    db.createVertexType(FanMarshaller.vertexClassName)
    db.createVertexType(BandMarshaller.vertexClassName)
    db.createVertexType(MusicianMarshaller.vertexClassName)

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
