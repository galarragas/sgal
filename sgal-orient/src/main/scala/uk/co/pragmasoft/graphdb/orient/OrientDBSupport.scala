package uk.co.pragmasoft.graphdb.orient

import com.tinkerpop.blueprints.{TransactionalGraph, Vertex}
import com.tinkerpop.blueprints.impls.orient._
import uk.co.pragmasoft.graphdb.GraphDAOSupport
import uk.co.pragmasoft.graphdb.marshalling.{GraphMarshallingDSL, GraphMarshaller}
import uk.co.pragmasoft.graphdb.validation.GraphDAOValidations

import scala.collection.JavaConversions._


trait OrientDBSupport[T] extends GraphDAOSupport[T] with OrientDBBasicConversions with GraphMarshallingDSL {

  self: GraphDAOValidations[T] =>

  def graphFactory: OrientGraphFactory

  override def createTransactionalGraph: TransactionalGraph = graphFactory.getTx()

  //https://groups.google.com/forum/#!topic/orient-database/9lnoOeN7Y3U
  protected def createQueryForNotIndexedProperty(implicit graph: OrientGraph): OrientGraphQuery = {
    graph.query().asInstanceOf[OrientGraphQuery]
  }


  def findVertexesOfClassByIndexedProperty(className: String, propertyName: String, value: Any)(implicit graph: OrientGraph): Iterator[Vertex] = {
    findWithIndex(s"$className.$propertyName", value)
  }

  def findWithIndex(indexFullName: String, value: Any)(implicit graph: OrientGraph): Iterator[Vertex] = {
    graph.getVertices(indexFullName, value).iterator()
  }

  def findByCompositeIndex(indexFullName: String, values: Any*)(implicit graph: OrientGraph): Iterator[Vertex] = {
    graph.getVertices(indexFullName, seqAsJavaList(values.toSeq) ).iterator()
  }

  def findByIndexedProperties(className: String, propertyNames: Iterable[String], values: Iterable[Any])(implicit graph: OrientGraph): Iterator[Vertex] = {
    graph.getVertices(
      className,
      Array(propertyNames.toList:_*),
      Array(  (values.map {_.asInstanceOf[java.lang.Object]} .toList):_*)
    ).iterator()
  }

  override def createNewVertex(id: IdType)(implicit graphDb: TransactionalGraph): Vertex =  {
    val orientGraph = graphDb.asInstanceOf[OrientGraph]

    orientGraph.addVertex(marshaller.vertexClassSpec, Array.empty[String]: _*)
  }

  def getRawById[T](id: String, graphDB: TransactionalGraph, marshaller : GraphMarshaller[T]): Option[Vertex] = {
    val theVertex: OrientVertex = graphDB.getVertex(id).asInstanceOf[OrientVertex]
    if (theVertex != null && theVertex.getVertexInstance.getVertexInstance.getLabel == marshaller.vertexClassName) {
      Some(theVertex)
    } else {
      None
    }
  }
}






