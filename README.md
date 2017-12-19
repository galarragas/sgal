# sgal: Scala Graph Access Layer

[![Build Status](https://api.travis-ci.org/galarragas/sgal.png)](http://travis-ci.org/galarragas/sgal)

## What is It?

The idea behind this project is to provide a set of common functionnalities and some very basic guidance when writing 
Data Access Objects for Graph Databases supporting the [Tinkerpop Blueprint API](https://github.com/tinkerpop/blueprints).

It is composed by a set of abstract interfaces and behavior and a custom implementations for [OrientDB](http://orientdb.com). The core package
works with [Titan](http://titan.thinkaurelius.com). See [the unit tests for Titan DB](./sgal-core/src/test/scala/uk/co/pragmasoft/graphdb/titan/TitanTestVertexDao.scala) 
for an example.

Using SGAL you'll automatically be able to have a full CRUD support for objects of type `T` in the DB at the cost of 
defining a **marshaller** for `T`

The responsibilities of the marshaller are to:

- Write and update the properties for the Vertexes storing type T, i.e. the attributes of the Vertex in the Graph
- Write and update the edges representing the relationships of Vertexes storing type T with other Vertexes

## What it does look like?

A Marshaller looks like the following code 

```scala
  object BandMarshaller extends OrientGraphMarshaller[Band] with OrientDBBasicConversions {
    type IdType = String

    override def vertexClassName: String = "artist"

    val PlaysIn = "plays-in"
    val NameAttribute = "name"
    val StylesAttribute = "styles"

    override def getModelObjectID(band: Band) = band.id

    override def read(vertex: Vertex)(implicit graphDb: TransactionalGraph): Band =
      Band(
        vertex.getId.asInstanceOf[ORID],
        vertex.getProperty[String](NameAttribute),
        vertex.getProperty(StylesAttribute).asInstanceOf[Set[String]],
        vertex.inAdjacentsForLabel[Musician](PlaysIn).toSet
      )

    override def propertiesForCreate(band: Band) = Set(
      NameAttribute -> band.name,
      StylesAttribute -> band.styles
    )

    // Cannot update name..
    override def propertiesForUpdate(band: Band) = Set(
      StylesAttribute -> band.styles
    )

    override def writeRelationships(artist: Band, vertex: Vertex)(implicit graphDb: TransactionalGraph) = {
      artist.musicians.foreach { musician =>
        vertex <-- PlaysIn <-- musician
      }
    }

    override def updateRelationships(band: Band, vertex: Vertex)(implicit graphDb: TransactionalGraph) = {
      vertex.removeEdges(PlaysIn, Direction.IN)

      writeRelationships(band, vertex)
    }
  }
```

## Directly Using the Marshaller

Once you defined the marshaller for your types you can start writing expressions as 
 
```scala
entity writeTo vertex

entity updateTo vertex

entity readFrom vertex

entity readMaybe vertexMaybe
```

## Creating a Data Access Object

Using the given marshaller you can create a CRUD supporting DAO with an extra query method with the following code. 
Note that the code is using some OrientDB specific features as the query by Lucene Index


```scala
class BandDao(override val graphFactory: OrientGraphFactory) extends OrientDbDAO[Band] with OrientIndexNamingSupport with OrientDBBasicConversions with NoValidations[Band] {

  override def marshaller = BandMarshaller
  
  def findByMusician(musician: Musician): Iterable[Band] = withGraphDb { implicit db =>
    vertexFor(musician).fold(Stream.empty[Band]) { vertex =>
      vertex.out(marshaller.PlaysIn)
        .map { _.as[Band] }
        .toStream()
    }
  }


  def findByPartialName(partialName: String): Iterable[Band] = withGraphDb { implicit db =>
    implicit val orientDb = db.asInstanceOf[OrientGraph]

    findByIndexedProperty(marshaller.NameAttribute, partialName).toIterable
  }
}
```

## Validation

The DAO trait is validating objects before writing and updates. Validation can be implemented as you wish and only need to throw an 
`IllegalArgumentException` when the object is considered invalid as per the `GraphDAOValidations` interface:

```scala
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
```

An already available implementation of the validation is based on the [ValiData project](https://github.com/galarragas/ValiData)
and allows you to specify the data validations with a simple DSL as per the example below:

```scala
object TestVertexValidator extends TypeValidator[TestVertex] with BaseValidations {
  override def validations = requiresAll(
    "Key" definedBy { _.key } must { beOfMinimumLength(3) and matchRegexOnce("[a-z]+.*".r) },
    "Property" definedBy { _.property } must bePositive[Int]
  )
}

class TitanTestVertexDao(graph: TitanGraph) extends GraphDAO[TestVertex] with ValiDataValidations[TestVertex]  {

  override protected def createTransactionalGraph: TransactionalGraph = graph

  override protected def newInstanceValidator: TypeValidator[TestVertex] = TestVertexValidator
  override protected def updatedInstanceValidator: TypeValidator[TestVertex] = TestVertexValidator

  override def marshaller = new TestVertexMarshaller

  // Returning a Stream here will fail because the objects would be read outside the transaction...
  def findByRelationship2(relatedObj: TestVertex): Iterable[TestVertex] = readWithGraphDb { implicit graph =>
    vertexFor(relatedObj).fold(List.empty[TestVertex]) { vertex =>
      vertex
        .out(TestVertexMarshaller.InBoundRelationship)
        .map( _.as[TestVertex] )
        .toList[TestVertex]
    }
  }
}
```

## OrientDB Support

The sub-project sgal-orient contains an extension of sgal-core with some specific features for Orient and some changes
in custom methods implementation to support some difference in the transaction support in Orient compared to Titan.

## License

Copyright 2014 PragmaSoft Ltd.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
