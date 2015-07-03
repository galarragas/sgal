package uk.co.pragmasoft.graphdb.orient

import com.orientechnologies.orient.core.id.{ORID, ORecordId}
import uk.co.pragmasoft.graphdb.orient.OrientDBBasicConversions.OridString

import scala.language.implicitConversions

trait OrientDBBasicConversions {
  implicit def oridAsString(orid: ORID): String = orid.toString
  implicit def stringAsORID(orid: String) = new ORecordId(orid)

  implicit def asOridString(str: String) = new OridString(str)
}

object OrientDBBasicConversions extends OrientDBBasicConversions {
  class OridString(val self: String) extends AnyVal {
    def asORID = stringAsORID(self)
  }
}