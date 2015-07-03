package uk.co.pragmasoft.graphdb.orient

import com.orientechnologies.orient.core.id.ORecordId
import org.scalatest.{Matchers, FlatSpec}

class OrientDBBasicConversionsSpec extends FlatSpec with Matchers with OrientDBBasicConversions {

  behavior of "OrientDBBasicConversion"

  it should "Convert a String to an Orient ID" in {
    "#1:10".asORID should be (new ORecordId("#1:10"))
  }

  it should "convert an orient id to string" in {
    def acceptString(value: String): String = value

    acceptString( new ORecordId("#1:10") ) should be("#1:10")
  }

}
