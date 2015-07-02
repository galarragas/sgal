package uk.co.pragmasoft.graphdb.validation

trait GraphDAOValidations[T] {
  /**
   * Validates an object before writing it as a new instance
   *
   * @param newInstance
   * @throws IllegalArgumentException if the object is invalid
   * @return
   */
  @throws[IllegalArgumentException]
  def validateNew(newInstance: T): T

  /**
   * Validates an object before writing it as an already existing instance
   *
   * @param updatedInstance
   * @throws IllegalArgumentException if the object is invalid
   * @return
   */
  @throws[IllegalArgumentException]
  def validateUpdate(updatedInstance: T): T
}






