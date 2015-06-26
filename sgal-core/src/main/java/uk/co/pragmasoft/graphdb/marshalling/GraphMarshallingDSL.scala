package uk.co.pragmasoft.graphdb.marshalling

import com.tinkerpop.blueprints.{TransactionalGraph, Direction, Vertex}
import uk.co.pragmasoft.graphdb.marshalling.GraphMarshallingDSL.{PimpedAny, PimpedVertex}
import scala.language.implicitConversions

trait GraphMarshallingDSL {
  implicit def pimpVertex(vertex: Vertex) = new PimpedVertex(vertex)
  implicit def marshallPimpAny[T](any: T) = new PimpedAny(any)
}

object GraphMarshallingDSL extends GraphMarshallingDSL{
   class PimpedVertex(val vertex: Vertex) extends AnyVal {
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

     /**
      * Creates an OUT edge with label @label from the given @vertex vertex to the target object @head
      * It assumes the @to vertex is present in the DB
      *
      * @throws IllegalArgumentException if the target vertex is not found
      */
     def addOutEdgeTo[EdgeHeadType](head: EdgeHeadType, label: String)(implicit graphDb: TransactionalGraph, targetMarshaller: GraphMarshaller[EdgeHeadType]) = {
       Option( graphDb.getVertex( targetMarshaller.getModelObjectID(head) ) ) match {
         case None =>
           throw new IllegalArgumentException(s"Unable to create edge from vertex $vertex to object $head, cannot retrieve associated vertex in DB")

         case Some(toVertex) =>
           vertex.addEdge(label, toVertex)
       }
     }

     /**
      * Creates an IN edge with label @label to the given @vertex vertex from the target object @tail
      * It assumes the @to vertex is present in the DB
      *
      * @throws IllegalArgumentException if the origin vertex is not found
      */
     def addInEdgeFrom[EdgeTailType](tail: EdgeTailType, label: String)(implicit graphDb: TransactionalGraph, targetMarshaller: GraphMarshaller[EdgeTailType]) = {
       Option( graphDb.getVertex( targetMarshaller.getModelObjectID(tail) ) ) match {
         case None =>
           throw new IllegalArgumentException(s"Unable to create edge to vertex $vertex form object $tail, cannot retrieve associated vertex in DB")

         case Some(fromVertex) =>
           fromVertex.addEdge(label, vertex)
       }
     }

     /**
      * Returns an iterable for the adjacents nodes of type EdgeHeadType navigating OUTPUT edges with label @label
      */
     def outAdjacentsForLabel[EdgeHeadType](label: String)(implicit reader: GraphMarshaller[EdgeHeadType], graph: TransactionalGraph): Iterable[EdgeHeadType] = {
       vertex.getVertices(Direction.OUT, label).map( _.as[EdgeHeadType] )
     }

     /**
      * Returns an iterable for the adjacents nodes of type EdgeHeadType navigating OUTPUT edges with label @label
      */
     def inAdjacentsForLabel[EdgeTailType](label: String)(implicit reader: GraphMarshaller[EdgeTailType], graph: TransactionalGraph): Iterable[EdgeTailType] = {
       vertex.getVertices(Direction.IN, label).map( _.as[EdgeTailType] )
     }
     
   }

   private[marshalling] class PimpedAny[T](val any: T) extends AnyVal {
     def getVertexId(implicit marshaller: GraphMarshaller[T]) = marshaller.getModelObjectID(any)

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