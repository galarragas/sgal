package uk.co.pragmasoft.graphdb

case class TestVertex(id: Long, key: String, property: Int, relationship1: Option[TestVertex], relationship2: Set[TestVertex])
