package uk.co.pragmasoft.graphdb.marshalling

import com.tinkerpop.blueprints.{TransactionalGraph, Vertex}

trait GraphMarshaller[T] extends GraphMarshallingDSL {
  type IdType
  
  /**
    * The class name for this vertex in Orient
    */
   def vertexClassName: String

   lazy val vertexClassSpec: String = s"class:$vertexClassName"

   /**
    * Writes data properties into the vertex properties. Not writing relationships
    *
    * @param data The model object
    * @param vertex The Orient DB Vertex
    * @param graphDb Reference to the Orient Gratph. The transaction is handled outside this method
    *
    * @return
    */
   def writeProperties(data: T, vertex: Vertex)(implicit graphDb: TransactionalGraph): Unit = {
      propertiesForCreate(data) foreach { property: (String, Any) =>
        vertex.setProperty(property._1, property._2)
      }
   }

  /**
   * As @writeProperties but in update mode. Might not write some of the properties
   *
   * @param data The model object
   * @param vertex The Orient DB Vertex
   * @param graphDb Reference to the Orient Gratph. The transaction is handled outside this method
   *
   * @return
   */
  def updateProperties(data: T, vertex: Vertex)(implicit graphDb: TransactionalGraph): Unit = {
    propertiesForUpdate(data) foreach { property: (String, Any) =>
      vertex.setProperty(property._1, property._2)
    }
  }

  /**
   * Extracts the properties to use in creation mode to be written into the associated vertex
   *
   * @param data
   * @return
   */
   def propertiesForCreate(data: T): Set[(String, Any)]

  /**
   * Extracts the properties to use in update mode to be updated into the associated vertex
   *
   * @param data
   * @return
   */
  def propertiesForUpdate(data: T): Set[(String, Any)]

   /**
    * Writes the properties of data mapped into relationships to other objects in the DB. Might create
    * associated objects
    *
    * @param data The model object
    * @param vertex The Orient DB Vertex
    * @param graphDb Reference to the Orient Gratph. The transaction is handled outside this method
    *
    * @return
    */
   def writeRelationships(data: T, vertex: Vertex)(implicit graphDb: TransactionalGraph): Unit



   /**
    * As @writeRelationships in update mode
    *
    * @param data The model object
    * @param vertex The Orient DB Vertex
    * @param graphDb Reference to the Orient Gratph. The transaction is handled outside this method
    *
    * @return
    */
   def updateRelationships(data: T, vertex: Vertex)(implicit graphDb: TransactionalGraph): Unit

   /**
    * Reads the content of Vertex @vertex (including relationships) into a new model object
    * @param vertex
    * @param graphDb
    * @return
    */
   def read(vertex: Vertex)(implicit graphDb: TransactionalGraph): T

  /**
   * Maps the content of an Option[Vertex] @vertex (including relationships) into a new Option of the model object
   * @param vertexMaybe
   * @param graphDb
   * @return
   */
  def readMaybe(vertexMaybe: Option[Vertex])(implicit graphDb: TransactionalGraph): Option[T] =  vertexMaybe map { read }

  /**
    * Extracts the OrientDB ID from a model object
    * @param obj
    * @return
    */
   def getModelObjectID(obj: T): IdType

 }
