package uk.co.pragmasoft.graphdb

import com.tinkerpop.blueprints.{TransactionalGraph, Vertex}
import uk.co.pragmasoft.graphdb.marshalling.{GraphMarshallingDSL, GraphMarshaller}
import uk.co.pragmasoft.graphdb.validation.GraphDAOValidations

trait GraphDAO[T] extends CrudDAO[T] with GraphMarshallingDSL {

  self: GraphDAOValidations[T] =>

  protected def createTransactionalGraph: TransactionalGraph

  def marshaller: GraphMarshaller[T]
  implicit val _marshaller = marshaller

  // FOR ORIENTDB THIS METHOD CAN BE USED ALSO NESTED
  // See https://github.com/orientechnologies/orientdb/wiki/Transaction-propagation about transaction propagation
  // Using pools: http://www.orientechnologies.com/new-orientdb-graph-factory/
  // We should be fine in using nested trasaction opening and committing local operations
  // https://github.com/orientechnologies/orientdb/wiki/Transactions
  protected def withGraphDb[T](block : TransactionalGraph=> T): T = {
    val graphDb = createTransactionalGraph

    try {
      val result = block(graphDb)
      graphDb.commit()

      result
    } catch {
      case e: Exception =>
        graphDb.rollback()
        throw e
    }
  }

  protected def readWithGraphDb[T](block : TransactionalGraph=> T): T = {
    val graphDb = createTransactionalGraph

    try {
      val result = block(graphDb)

      result
    } finally {
      graphDb.shutdown()
    }
  }

  override def create(newInstance: T): T = {
     validateNew(newInstance)

     withGraphDb { implicit graphDB =>
       val newVertex = createNewVertex(_marshaller.getModelObjectID(newInstance))
       newInstance writeTo newVertex

       newVertex.as[T]
     }
   }

  override def update(existingInstance: T): T = {
    validateUpdate(existingInstance)

    withGraphDb { implicit graphDB =>
      val id = _marshaller.getModelObjectID(existingInstance)

      val vertexOp = getAsVertexById(id)
      require(vertexOp.isDefined, s"unable to update entity with Id ${existingInstance.getVertexId}. Not in the DB")

      val vertex = vertexOp.get

      existingInstance updateTo vertex

      vertex.as[T]
    }
  }

  override def delete(existingInstance: T): Boolean = deleteById(_marshaller.getModelObjectID(existingInstance))

  def deleteById(id: _marshaller.IdType): Boolean = withGraphDb { implicit graphDB =>
    getAsVertexById(id) match {
      case Some(vertex) =>
        graphDB.removeVertex(vertex)
        true
      case None =>
        false
    }
  }

  def getById(id: Any): Option[T] = readWithGraphDb { implicit graphDB =>
    getAsVertexById(id) map { _.as[T]  }
  }

  def getAsVertexById(id: Any)(implicit graphDB: TransactionalGraph): Option[Vertex] = {
    val v = graphDB.getVertex(id)
    Option(v)
  }

  protected def createNewVertex(id: Any)(implicit graphDb: TransactionalGraph): Vertex = graphDb.addVertex(id)

  protected def vertexFor[VertexType](element: VertexType)(implicit elementMarshaller: GraphMarshaller[VertexType], graphDb: TransactionalGraph): Option[Vertex] =
    Option( graphDb.getVertex(element.getVertexId) )
}
