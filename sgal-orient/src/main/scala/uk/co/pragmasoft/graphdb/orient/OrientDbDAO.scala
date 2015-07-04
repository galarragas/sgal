package uk.co.pragmasoft.graphdb.orient

import com.tinkerpop.blueprints.{Query, TransactionalGraph, Vertex}
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

  /**
   * Query entities of the vertex class using OrientDB custom features
   * See https://groups.google.com/forum/#!topic/orient-database/9lnoOeN7Y3U
   * 
   * @param graph
   *              
   * @return a query object with an already set filter on entites of the handled vertex class type
   */
  protected def queryForEntityClass(implicit graph: TransactionalGraph): Query = {
    graph.query().asInstanceOf[OrientGraphQuery].labels(marshaller.vertexClassName)
  }

  /**
   * Collects a vertex OF THE HANDLED VERTEX CLASS given the specified ID
   * 
   * @param id
   * @param graphDB
   *
   * @return Some(vertex) if the vertex is found and is of the right class type, None otherwise 
   */
  override def getAsVertexById(id: Any)(implicit graphDB: TransactionalGraph): Option[Vertex] = {
    val theVertex: OrientVertex = graphDB.getVertex(id).asInstanceOf[OrientVertex]
    if (theVertex != null && theVertex.getVertexInstance.getVertexInstance.getLabel == marshaller.vertexClassName) {
      Some(theVertex)
    } else {
      None
    }
  }


  /**
   * Find vertices of the given class by the given property name. The property is expected to have been indexed
   * According to OrientDB class index strategy.
   */
  protected def findByIndexedProperty(className: String, propertyName: String, value: Any)(implicit graph: TransactionalGraph): Iterator[Vertex] = {
    findWithIndex(s"$className.$propertyName", value)
  }


  protected def findByIndexedProperty(propertyName: String, value: Any)(implicit graph: TransactionalGraph): Iterator[T] =
    findByIndexedProperty(marshaller.vertexClassName, propertyName, value).map(_.as[T])


  /**
   * As findByIndexedProperty for many indexed properties
   */
  protected def findByIndexedProperties(className: String, propertyNames: Iterable[String], values: Iterable[Any])(implicit graph: OrientGraph): Iterator[Vertex] = {
    graph.getVertices(
      className,
      Array(propertyNames.toList:_*),
      Array(  (values.map {_.asInstanceOf[java.lang.Object]} .toList):_*)
    ).iterator()
  }

  protected def findByIndexedProperties(propertyNames: Iterable[String], values: Iterable[Any])(implicit graph: OrientGraph): Iterator[T] = {
    findByIndexedProperties(marshaller.vertexClassName, propertyNames, values).map(_.as[T])

  }

  /**
   * Finds vertices using orient's index with the given name
   */
  protected def findWithIndex(indexFullName: String, value: Any)(implicit graph: TransactionalGraph): Iterator[Vertex] = {
    graph.getVertices(indexFullName, value).iterator()
  }

  /**
   * Finds vertices using orient's composite index with the given name
   */
  protected def findByCompositeIndex(indexFullName: String, values: Any*)(implicit graph: TransactionalGraph): Iterator[Vertex] = {
    graph.getVertices(indexFullName, seqAsJavaList(values.toSeq) ).iterator()
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

  
}






