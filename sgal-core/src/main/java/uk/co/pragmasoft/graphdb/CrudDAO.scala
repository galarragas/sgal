package uk.co.pragmasoft.graphdb

trait CrudDAO[T] {
  def create(obj: T): T
  def update(obj: T): T
  def delete(obj: T): Boolean

  def getById(id: String): Option[T]
}
