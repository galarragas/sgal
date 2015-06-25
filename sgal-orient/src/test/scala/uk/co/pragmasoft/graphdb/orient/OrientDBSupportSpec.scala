package uk.co.pragmasoft.graphdb.orient

import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx
import org.scalatest.{Matchers, FlatSpec}
import uk.co.pragmasoft.graphdb.orient.support.OrientDBLocalTestSupport


class OrientDBSupportSpec extends FlatSpec with Matchers with OrientDBLocalTestSupport {



  override def initDB(db: OrientGraphNoTx): Unit = {

  }
}
