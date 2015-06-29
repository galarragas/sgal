package uk.co.pragmasoft.graphdb.orient.sampledao


case class Fan(id: String, name: String, age: Int, fanOf: Set[Band])

case class Band(id: String, name: String, styles: Set[String], musicians: Set[Musician])

case class Musician(id: String, name: String, instrument: String)



