package uk.co.pragmasoft.graphdb.orient

import uk.co.pragmasoft.graphdb.orient.OrientGraphDSL.PimpedOrientAny
import scala.language.implicitConversions

trait OrientGraphDSL {
    implicit def toPrintedOrientAny[T](any: T) = new PimpedOrientAny(any)
}

object OrientGraphDSL {
  class PimpedOrientAny[T](val any: T) extends AnyVal {
    def vertexClassName(implicit marshaller: OrientGraphMarshaller[T]) = marshaller.vertexClassName
    def vertexClassSpec(implicit marshaller: OrientGraphMarshaller[T]) = marshaller.vertexClassSpec
  }
}