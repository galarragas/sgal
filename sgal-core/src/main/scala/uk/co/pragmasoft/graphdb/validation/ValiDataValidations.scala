package uk.co.pragmasoft.graphdb.validation


import uk.co.pragmasoft.validate.TypeValidator

import scalaz._

trait ValiDataValidations[T] extends GraphDAOValidations[T] {
  def newInstanceValidator: TypeValidator[T]
  
  def updatedInstanceValidator: TypeValidator[T]

  private def printValidationErrors(validationErrors: NonEmptyList[String]): String = validationErrors.list.mkString("[", ",", "]")

  @throws[IllegalArgumentException]
  override def validateNew(newInstance: T): T = 
    newInstanceValidator(newInstance).valueOr {
      validationErrors => throw new IllegalArgumentException(s"Invalid data for new instance $newInstance. Validation errors ${printValidationErrors(validationErrors)}")
    }
  

  @throws[IllegalArgumentException]
  override def validateUpdate(updatedInstance: T): T =
    updatedInstanceValidator(updatedInstance).valueOr {
      validationErrors => throw new IllegalArgumentException(s"Invalid data for new instance $updatedInstance. Validation errors ${printValidationErrors(validationErrors)}")
    }
}
