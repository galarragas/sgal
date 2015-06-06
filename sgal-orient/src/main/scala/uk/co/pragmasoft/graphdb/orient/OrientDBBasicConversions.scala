package uk.co.pragmasoft.graphdb.orient

import com.orientechnologies.orient.core.id.{ORID, ORecordId}

import scala.language.implicitConversions

trait OrientDBBasicConversions {
  implicit def oridAsString(orid: ORID): String = orid.toString
  implicit def stringAsORID(orid: String) = new ORecordId(orid)

  implicit class OridString(self: String) {
    def asORID = stringAsORID(self)
  }
}

object OrientDBBasicConversions extends OrientDBBasicConversions