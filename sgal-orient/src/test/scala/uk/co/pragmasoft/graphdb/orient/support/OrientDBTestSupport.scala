package uk.co.pragmasoft.graphdb.orient.support

import java.io._
import java.nio.file.{Files, Path}

import com.tinkerpop.blueprints.impls.orient.{OrientGraphFactory, OrientGraph, OrientGraphNoTx}
import org.apache.commons.lang.RandomStringUtils

trait OrientDBMemoryTestSupport {

  def initDB(db: OrientGraphNoTx): Unit

  def withInMemoryOrientGraphDB[T]( block: OrientGraphFactory =>  T): T = {
    val testDBName = RandomStringUtils.randomAlphabetic(10)
    val orientGraphFactory = new OrientGraphFactory(s"memory:$testDBName").setupPool(1,5)
    val initFactory = new OrientGraphFactory(s"memory:$testDBName") //.setTransactional(false)


    test(block, orientGraphFactory, initFactory)
  }

  def withLocalOrientGraphDB[T]( block: OrientGraphFactory =>  T): T = {
    val testDBPath = RandomStringUtils.randomAlphabetic(10)
    val orientGraphFactory = new OrientGraphFactory(s"plocal:$testDBPath").setupPool(1,5)
    val initFactory = new OrientGraphFactory(s"plocal:$testDBPath") //.setTransactional(false)

    test(block, orientGraphFactory, initFactory)

  }

  def test[T](block: (OrientGraphFactory) => T, orientGraphFactory: OrientGraphFactory, initFactory: OrientGraphFactory): T = {
    val dbForInit = initFactory.getNoTx

    try {
      initDB(dbForInit)
      dbForInit.commit()
    } finally {
      dbForInit.shutdown()
    }

    try {
      block(orientGraphFactory)
    } finally {
      orientGraphFactory.drop()
      orientGraphFactory.close()
      initFactory.close()
    }
  }
  
  def withinTx[T](block: OrientGraph => T)(implicit graphFactory: OrientGraphFactory) : T= {
    val graph = graphFactory.getTx
    try {
      block(graph)
    } finally {
      graph.shutdown()
    }
  }
}

trait OrientDBLocalTestSupport {

  def initDB(db: OrientGraphNoTx): Unit

  val testDBPath = Files.createTempDirectory( s"orientdbtest" )

  def removeAll(path: Path) = {
    def getRecursively(f: File): Seq[File] =
      f.listFiles.filter(_.isDirectory).flatMap(getRecursively) ++ f.listFiles

    getRecursively(path.toFile).foreach{f =>
      if (!f.delete())
        throw new RuntimeException("Failed to delete " + f.getAbsolutePath)}
  }

  def withLocalOrientGraphDB[T]( block: OrientGraphFactory =>  T): T = {

    val dbPath = s"plocal:$testDBPath"
    val orientGraphFactory = new OrientGraphFactory(dbPath).setupPool(1,5)
    val initFactory = new OrientGraphFactory(dbPath)

    test(block, orientGraphFactory, initFactory)
  }


  def test[T](block: (OrientGraphFactory) => T, orientGraphFactory: OrientGraphFactory, initFactory: OrientGraphFactory): T = {
    val dbForInit = initFactory.getNoTx

    try {
      initDB(dbForInit)
      dbForInit.commit()
    } finally {
      dbForInit.shutdown()

    }

    try {
      block(orientGraphFactory)
    } finally {
      orientGraphFactory.drop()
      orientGraphFactory.close()
      initFactory.close()

      removeAll(testDBPath )
    }
  }
}


