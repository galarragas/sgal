package uk.co.pragmasoft.graphdb

import com.tinkerpop.blueprints.TransactionalGraph
import uk.co.pragmasoft.graphdb.marshalling.GraphMarshaller
import uk.co.pragmasoft.graphdb.validation.GraphDAOValidations


class DITestVertexDao(val graph: TransactionalGraph, override val marshaller: GraphMarshaller[TestVertex]) extends GraphDAO[TestVertex] {
  self: GraphDAOValidations[TestVertex] =>

  override protected def createTransactionalGraph: TransactionalGraph = graph

}
