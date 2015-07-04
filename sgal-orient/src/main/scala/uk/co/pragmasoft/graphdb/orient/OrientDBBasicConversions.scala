package uk.co.pragmasoft.graphdb.orient

import com.orientechnologies.orient.core.id.{ORID, ORecordId}
import com.orientechnologies.orient.core.record.ORecord
import com.tinkerpop.blueprints.Vertex
import uk.co.pragmasoft.graphdb.orient.OrientDBBasicConversions.OridString
import uk.co.pragmasoft.graphdb.orient.OrientGraphMarshallingDSL.PimpedOrientVertex
import scala.collection.JavaConversions._
import scala.language.implicitConversions

trait OrientDBBasicConversions {
  implicit def oridAsString(orid: ORID): String = orid.toString
  implicit def stringAsORID(orid: String) = new ORecordId(orid)

  implicit def asOridString(str: String) = new OridString(str)
}

trait OrientGraphMarshallingDSL {
  implicit def pimpOrientVertex(vertex: Vertex) = new PimpedOrientVertex(vertex)
}

object OrientDBBasicConversions extends OrientDBBasicConversions {
  class OridString(val self: String) extends AnyVal {
    def asORID = stringAsORID(self)
  }
}

object OrientGraphMarshallingDSL extends OrientGraphMarshallingDSL {
  class PimpedOrientVertex(val vertex: Vertex) extends AnyRef {
    def embeddedListProperty[T](name: String): Option[List[T]] =
      Option(vertex.getProperty(name)).map { property: AnyRef =>
        property
          .asInstanceOf[java.util.List[T]]
          .toList
      }


    def embeddedSetProperty[T](name: String): Option[Set[T]] =
      Option(vertex.getProperty(name)).map { property: AnyRef =>
        property
          .asInstanceOf[java.util.Set[T]]
          .toSet
      }

    def embeddedMapProperty[K, V](name: String): Option[Map[K, V]] =
      Option(vertex.getProperty(name)).map { property: AnyRef =>
        property
          .asInstanceOf[java.util.Map[K, V]]
          .toMap
      }

    def embeddedRecordProperty[K, V](name: String): Option[ORecord] =
      Option(vertex.getProperty(name)).map { property: AnyRef =>
        property
          .asInstanceOf[ORecord]
      }
  }
}