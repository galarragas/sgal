# sgal: Scala Graph Access Layer

## What is It?

The idea behind this project is to provide a set of common functionnalities and some very basic guidance when writing 
Data Access Objects for Graph Databases supporting the Tinkerpop Blueprint API.

It is composed by a set of abstract interfaces and behavior and implementations for different databases 
(at the moment just OrientDB)


Using SGAL you'll automatically be able to have a full CRUD support for objects of type `T` in the DB at the cost of 
defining a marshaller for `T`

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

    override def getModelObjectID(artist: Band) = artist.id

    override def read(vertex: Vertex)(implicit graphDb: TransactionalGraph): Band =
      Band(
        vertex.getId.asInstanceOf[ORID],
        vertex.getProperty[String](NameAttribute),
        vertex.getProperty(StylesAttribute).asInstanceOf[Set[String]],
        vertex.inAdjacentsForLabel[Musician](PlaysIn).toSet
      )

    override def propertiesForCreate(artist: Band) = Set(
      NameAttribute -> artist.name,
      StylesAttribute -> artist.styles
    )

    // Cannot update name..
    override def propertiesForUpdate(artist: Band) = Set(
      StylesAttribute -> artist.styles
    )

    override def writeRelationships(artist: Band, vertex: Vertex)(implicit graphDb: TransactionalGraph) = {
      artist.musicians.foreach { musician =>
        vertex.addInEdgeFrom(musician, PlaysIn)
      }
    }

    override def updateRelationships(artist: Band, vertex: Vertex)(implicit graphDb: TransactionalGraph) = {
      vertex.removeEdges(PlaysIn, Direction.IN )

      artist.musicians.foreach { musician =>
        vertex <-- PlaysIn <-- musician
      }
    }
  }
```

Using the given marshaller you can create a CRUD supporting DAO with an extra query method with the following code (OrientDB specific)

```scala
class BandDao(override val graphFactory: OrientGraphFactory) extends OrientDbDAO[Band] with OrientIndexNamingSupport with OrientDBBasicConversions with NoValidations[Band] {

  override def marshaller = BandMarshaller
  
  def findByMusician(musician: Musician): Iterable[Band] = withGraphDb { implicit db =>
    vertexFor(musician)
      .out(marshaller.PlaysIn)
      .map { vertex => vertex.as[Band] }
      .toStream()
  }

}
```

## License

Copyright 2014 PragmaSoft Ltd.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0