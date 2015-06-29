package uk.co.pragmasoft.graphdb.validation

/**
 * Created by stefano on 06/06/15.
 */
trait NoValidations[T] extends GraphDAOValidations[T] {

  @throws[IllegalArgumentException]
  def validateNew(newInstance: T): Unit = {}

  @throws[IllegalArgumentException]
  def validateUpdate(existingInstance: T): Unit = {}

}
