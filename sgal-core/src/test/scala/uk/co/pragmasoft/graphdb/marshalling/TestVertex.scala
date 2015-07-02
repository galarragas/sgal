package uk.co.pragmasoft.graphdb.marshalling

case class TestVertex(key: String, property: Int, relationship1: Option[TestVertex], relationship2: Set[TestVertex])
