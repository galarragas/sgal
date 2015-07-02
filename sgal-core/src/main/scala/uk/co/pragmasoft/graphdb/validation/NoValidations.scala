package uk.co.pragmasoft.graphdb.validation

trait NoValidations[T] extends GraphDAOValidations[T] {

  @throws[IllegalArgumentException]
  override def validateNew(newInstance: T) = newInstance

  @throws[IllegalArgumentException]
  override def validateUpdate(existingInstance: T) = existingInstance

}
