package edu.ucdavis.fiehnlab.ctsRest.client

import com.typesafe.scalalogging.LazyLogging
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

@RunWith(classOf[SpringRunner])
@SpringBootTest
class CtsRestClientTest extends WordSpec with LazyLogging with ShouldMatchers {
  @Autowired
  val ctsRestClient: CtsRestClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "CtsRestClientTest" should {
    "conver inchi 2 Mol" in {
      ctsRestClient.inchiCode2InchiKey("InChI=1S/H2O/h1H2") should equal("XLYOFNOQVPJJNP-UHFFFAOYSA-N")
    }

    "smiles2inchi" in { }

    "mol2Inchi" in { }

    "inchiCode2InchiKey" in { }
  }
}
