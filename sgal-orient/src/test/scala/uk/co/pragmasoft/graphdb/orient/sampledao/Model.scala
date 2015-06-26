package uk.co.pragmasoft.graphdb.orient.sampledao


case class Fan(id: String, name: String, age: Int, fanOf: Seq[Artist])

case class Artist(id: String, name: String, styles: Seq[String], musicians: Seq[Musician])

case class Musician(id: String, name: String, instrument: String)



