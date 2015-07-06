package uk.co.pragmasoft.graphdb.titan

import com.thinkaurelius.titan.core.TitanGraph
import com.tinkerpop.blueprints.TransactionalGraph
import uk.co.pragmasoft.graphdb.{GraphDAO, TestVertex}
import uk.co.pragmasoft.graphdb.marshalling.TestVertexMarshaller
import uk.co.pragmasoft.graphdb.validation.ValiDataValidations
import uk.co.pragmasoft.validate._
import com.tinkerpop.gremlin.scala._

object TestVertexValidator extends TypeValidator[TestVertex] with BaseValidations {
  override def validations = requiresAll(
    "Key" definedBy { _.key } must { beOfMinimumLength(3) and matchRegexOnce("[a-z]+.*".r) },
    "Property" definedBy { _.property } must bePositive[Int]
  )
}

class TitanTestVertexDao(graph: TitanGraph) extends GraphDAO[TestVertex] with ValiDataValidations[TestVertex]  {

  override protected def createTransactionalGraph: TransactionalGraph = graph

  override protected def newInstanceValidator: TypeValidator[TestVertex] = TestVertexValidator
  override protected def updatedInstanceValidator: TypeValidator[TestVertex] = TestVertexValidator

  override def marshaller = new TestVertexMarshaller

  // Returning a Stream here will fail because the objects would be read outside the transaction...
  def findByRelationship2(relatedObj: TestVertex): Iterable[TestVertex] = readWithGraphDb { implicit graph =>
    vertexFor(relatedObj).fold(List.empty[TestVertex]) { vertex =>
      vertex
        .out(TestVertexMarshaller.InBoundRelationship)
        .map( _.as[TestVertex] )
        .toList[TestVertex]
    }
  }
}
