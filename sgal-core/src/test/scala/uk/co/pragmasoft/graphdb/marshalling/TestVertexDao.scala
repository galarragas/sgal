package uk.co.pragmasoft.graphdb.marshalling

import com.tinkerpop.blueprints.TransactionalGraph
import uk.co.pragmasoft.graphdb.GraphDAO
import uk.co.pragmasoft.graphdb.validation.GraphDAOValidations


class TestVertexDao(val graph: TransactionalGraph, override val marshaller: GraphMarshaller[TestVertex]) extends GraphDAO[TestVertex] {
  self: GraphDAOValidations[TestVertex] =>

  override protected def createTransactionalGraph: TransactionalGraph = graph

}
