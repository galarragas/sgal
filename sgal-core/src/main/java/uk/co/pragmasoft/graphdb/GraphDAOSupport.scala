package uk.co.pragmasoft.graphdb

import com.tinkerpop.blueprints.{TransactionalGraph, Vertex}
import com.tinkerpop.blueprints.impls.orient.{OrientVertex, OrientGraph}
import net.sf.aspect4log.slf4j.LoggerFactory
import org.fannan.bigdata.core.inventory.persistence.orientdb.support.validation.OrientDAOValidations
import org.fannan.bigdata.core.inventory.persistence.orientdb.support.validation.scalazed.BaseScalazValidations
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

  def getRawById[T](id: String, graphDB: TransactionalGraph, marshaller : GraphMarshaller[T]): Option[Vertex] =
    Option(graphDB.getVertex(id))


  @throws[IllegalArgumentException]
  def validateNew(newInstance: T): Unit

  @throws[IllegalArgumentException]
  def validateUpdate(existingInstance: T): Unit

   def create(newInstance: T): T = {
     validateNew(newInstance)

     withGraphDb { implicit graphDB =>
       val newVertex = createNewVertex
       newInstance write newVertex

       // Need to close the first transaction to have the ID created valid
       graphDB.commit()

       newVertex.as[T]
     }
   }

  def update(existingInstance: T): T = {
    validateUpdate(existingInstance)

    val updatedVertex = withGraphDb { implicit graphDB =>
      val id = existingInstance.getOrientID

      val vertexOp = getRawById(id)
      require(vertexOp.isDefined, s"unable to update entity with Id ${existingInstance.getOrientID}. Not in the DB")

      val vertex = vertexOp.get

      existingInstance update vertex

      vertex
    }

    readWithGraphDb { implicit graphDB =>
      updatedVertex.as[T]
    }
  }

  def delete(id: String): Boolean = withGraphDb { implicit graphDB =>
    getRawById(id) match {
      case Some(vertex) =>
        graphDB.removeVertex(vertex)
        true
      case None =>
        false
    }
  }

  def delete(existingInstance: T): Boolean = delete(existingInstance.getOrientID)

  def getById(id: String): Option[T] = readWithGraphDb { implicit graphDB =>
    getRawById(id) map { _.as[T]  }
  }

  def getRawById(id: String)(implicit graphDB: TransactionalGraph): Option[Vertex] = {
    GraphDAOSupport.getRawById(id, graphDB, marshaller)
  }

  def createNewVertex(implicit graphDb: TransactionalGraph): Vertex = graphDb.addVertex(marshaller.vertexClassSpec, Array.empty[String]: _*)

  def findByPropertyValue(propertyName: String, value: Any)(implicit graph: TransactionalGraph): Iterator[Vertex] = {
    // Trying to use the indexed property, if the index is not present the query will work anyway with full scan
    findByClassIndex(propertyName, value)
  }

  def findByClassIndex(indexName: String, value: Any)(implicit graph: TransactionalGraph): Iterator[Vertex] = {
    findWithIndex(s"${_marshaller.vertexClassName}.$indexName", value)
  }

  def findByIndexedProperties(propertyNames: Iterable[String], values: Iterable[Any])(implicit graph: TransactionalGraph): Iterator[Vertex] = {
    findByIndexedProperties(_marshaller.vertexClassName, propertyNames.toList, values )
  }

  def findByCompositeClassIndex(indexName: String, values: Any*)(implicit graph: TransactionalGraph): Iterator[Vertex] = {
    findByCompositeIndex(s"${_marshaller.vertexClassName}.$indexName", values:_*)
  }
 }



