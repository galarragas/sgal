package uk.co.pragmasoft.graphdb.titan

import com.thinkaurelius.titan.core.{TitanFactory, TitanGraph}


trait InMemoryTitanDao  {

  def withTitanDao[T](block: TitanGraph => T): T = {
    val graph = TitanFactory.build().set( "storage.backend","inmemory").open()

    try {
      block(graph)
    } finally {
      if(graph.isOpen)
        graph.shutdown()
    }

  }

}
