package uk.co.pragmasoft.graphdb

import com.tinkerpop.blueprints.{TransactionalGraph, Vertex}
import uk.co.pragmasoft.graphdb.marshalling.GraphMarshaller
import uk.co.pragmasoft.graphdb.validation.GraphDAOValidations

trait GraphDAOSupport[T] extends CrudDAO[T]  {

  self: GraphDAOValidations[T] =>

  def createTransactionalGraph: TransactionalGraph

  import uk.co.pragmasoft.graphdb.marshalling.GraphMarshallingDSL._

  def marshaller: GraphMarshaller[T]
  implicit val _marshaller = marshaller

  // FOR ORIENTDB THIS METHOD CAN BE USED ALSO NESTED
  // See https://github.com/orientechnologies/orientdb/wiki/Transaction-propagation about transaction propagation
  // Using pools: http://www.orientechnologies.com/new-orientdb-graph-factory/
  // We should be fine in using nested trasaction opening and committing local operations
  // https://github.com/orientechnologies/orientdb/wiki/Transactions
  def withGraphDb[T](block : TransactionalGraph=> T): T = {
    val graphDb = createTransactionalGraph

    try {
      val result = block(graphDb)
      graphDb.commit()

      result
    } catch {
      case e: Exception =>
        graphDb.rollback()
        throw e
    } finally {
      graphDb.shutdown()
    }
  }

  def readWithGraphDb[T](block : TransactionalGraph=> T): T = {
    val graphDb = createTransactionalGraph

    try {
      val result = block(graphDb)

      result
    } finally {
      graphDb.shutdown()
    }
  }

  @throws[IllegalArgumentException]
  def validateNew(newInstance: T): Unit

  @throws[IllegalArgumentException]
  def validateUpdate(existingInstance: T): Unit

   def create(newInstance: T): T = {
     validateNew(newInstance)

     withGraphDb { implicit graphDB =>
       val newVertex = createNewVertex(marshaller.getModelObjectID(newInstance))
       newInstance write newVertex

       // Need to close the first transaction to have the ID created valid
       graphDB.commit()

       newVertex.as[T]
     }
   }

  def update(existingInstance: T): T = {
    validateUpdate(existingInstance)

    val updatedVertex = withGraphDb { implicit graphDB =>
      val id = existingInstance.getVertexId

      val vertexOp = getRawById(id)
      require(vertexOp.isDefined, s"unable to update entity with Id ${existingInstance.getVertexId}. Not in the DB")

      val vertex = vertexOp.get

      existingInstance update vertex

      vertex
    }

    readWithGraphDb { implicit graphDB =>
      updatedVertex.as[T]
    }
  }

  def delete(id: _marshaller.IdType): Boolean = withGraphDb { implicit graphDB =>
    getRawById(id) match {
      case Some(vertex) =>
        graphDB.removeVertex(vertex)
        true
      case None =>
        false
    }
  }

  def delete(existingInstance: T): Boolean = delete(existingInstance.getVertexId)

  def getById(id: _marshaller.IdType): Option[T] = readWithGraphDb { implicit graphDB =>
    getRawById(id) map { _.as[T]  }
  }

  def getRawById(id: _marshaller.IdType)(implicit graphDB: TransactionalGraph): Option[Vertex] =  Option( graphDB.getVertex(id))

  def createNewVertex(id: Any)(implicit graphDb: TransactionalGraph): Vertex = graphDb.addVertex(marshaller.vertexClassSpec, Array.empty[String]: _*)

}
