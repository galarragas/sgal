package uk.co.pragmasoft.graphdb.orient

import com.tinkerpop.blueprints.{TransactionalGraph, Vertex}
import com.tinkerpop.blueprints.impls.orient._
import uk.co.pragmasoft.graphdb.GraphDAO
import uk.co.pragmasoft.graphdb.marshalling.{GraphMarshallingDSL, GraphMarshaller}
import uk.co.pragmasoft.graphdb.validation.GraphDAOValidations
import uk.co.pragmasoft.graphdb.marshalling.GraphMarshallingDSL

import scala.collection.JavaConversions._


trait OrientDbDAO[T] extends GraphDAO[T] with OrientDBBasicConversions with GraphMarshallingDSL {

  self: GraphDAOValidations[T] =>

  override def marshaller: OrientGraphMarshaller[T]

  def graphFactory: OrientGraphFactory

  override protected def createTransactionalGraph: TransactionalGraph = graphFactory.getTx()

  //https://groups.google.com/forum/#!topic/orient-database/9lnoOeN7Y3U
  protected def createQueryForNotIndexedProperty(implicit graph: OrientGraph): OrientGraphQuery = {
    graph.query().asInstanceOf[OrientGraphQuery]
  }


  protected def findVertexesOfClassByIndexedProperty(className: String, propertyName: String, value: Any)(implicit graph: OrientGraph): Iterator[Vertex] = {
    findWithIndex(s"$className.$propertyName", value)
  }

  protected def findWithIndex(indexFullName: String, value: Any)(implicit graph: OrientGraph): Iterator[Vertex] = {
    graph.getVertices(indexFullName, value).iterator()
  }

  protected def findByCompositeIndex(indexFullName: String, values: Any*)(implicit graph: OrientGraph): Iterator[Vertex] = {
    graph.getVertices(indexFullName, seqAsJavaList(values.toSeq) ).iterator()
  }

  protected def findByIndexedProperties(className: String, propertyNames: Iterable[String], values: Iterable[Any])(implicit graph: OrientGraph): Iterator[Vertex] = {
    graph.getVertices(
      className,
      Array(propertyNames.toList:_*),
      Array(  (values.map {_.asInstanceOf[java.lang.Object]} .toList):_*)
    ).iterator()
  }


  /**
   * The ID in Orient can be used to specify the vertex class type
   *
   * @param id
   * @param graphDb
   * @return
   */
  override protected def createNewVertex(id: Any)(implicit graphDb: TransactionalGraph): Vertex =  {
    val orientGraph = graphDb.asInstanceOf[OrientGraph]

    orientGraph.addVertex(marshaller.vertexClassSpec, Array.empty[String]: _*)
  }

  protected def getRawById[T](id: String, graphDB: TransactionalGraph): Option[Vertex] = {
    val theVertex: OrientVertex = graphDB.getVertex(id).asInstanceOf[OrientVertex]
    if (theVertex != null && theVertex.getVertexInstance.getVertexInstance.getLabel == marshaller.vertexClassName) {
      Some(theVertex)
    } else {
      None
    }
  }
}






