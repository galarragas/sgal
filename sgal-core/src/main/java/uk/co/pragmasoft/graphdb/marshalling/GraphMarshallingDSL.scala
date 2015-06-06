package uk.co.pragmasoft.graphdb.marshalling

import com.tinkerpop.blueprints.{TransactionalGraph, Direction, Vertex}


object GraphMarshallingDSL {
   implicit class PimpedVertex(val vertex: Vertex) extends AnyVal {
     import scala.collection.JavaConversions._

     def as[T](implicit reader: GraphMarshaller[T], graph: TransactionalGraph) : T =  reader.read(vertex)(graph)

     def addEdges(label: String, inVertexes: Vertex*) = inVertexes.map(vertex.addEdge(label, _))

     def removeEdges(label: String, direction: Direction = Direction.OUT) = {
       val edges = vertex.getEdges(direction, label)
       val iterator = edges.iterator
       for(edge <- iterator) {
           edge.remove
       }
     }
   }

   private[orientdb] implicit class PimpedAny[T](val any: T) extends AnyVal {
     def getOrientID(implicit marshaller: GraphMarshaller[T]) = marshaller.getModelObjectID(any)

     def vertexClassName(implicit marshaller: GraphMarshaller[T]) = marshaller.vertexClassName
     def vertexClassSpec(implicit marshaller: GraphMarshaller[T]) = marshaller.vertexClassSpec

     def write(vertex: Vertex)(implicit marshaller: GraphMarshaller[T], graphDb: TransactionalGraph): Unit = {
       marshaller.writeProperties(any, vertex)
       marshaller.writeRelationships(any, vertex)
     }

     def update(vertex: Vertex)(implicit marshaller: GraphMarshaller[T], graphDb: TransactionalGraph): Unit = {
       marshaller.updateProperties(any, vertex)
       marshaller.updateRelationships(any, vertex)
     }

     def writeProperties(vertex: Vertex)(implicit marshaller: GraphMarshaller[T], graphDb: TransactionalGraph): Unit = {
       marshaller.writeProperties(any, vertex)
     }

     def writeRelationships(vertex: Vertex)(implicit marshaller: GraphMarshaller[T], graphDb: TransactionalGraph): Unit = {
       marshaller.writeRelationships(any, vertex)
     }

     def updateProperties(vertex: Vertex)(implicit marshaller: GraphMarshaller[T], graphDb: TransactionalGraph): Unit = {
       marshaller.updateProperties(any, vertex)
     }

     def updateRelationships(vertex: Vertex)(implicit marshaller: GraphMarshaller[T], graphDb: TransactionalGraph): Unit = {
       marshaller.updateRelationships(any, vertex)
     }
   }

 }
