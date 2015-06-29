package uk.co.pragmasoft.graphdb.validation

trait GraphDAOValidations[T] {
  @throws[IllegalArgumentException]
  def validateNew(newInstance: T): Unit

  @throws[IllegalArgumentException]
  def validateUpdate(existingInstance: T): Unit
}






