package edu.ucdavis.fiehnlab.ctsrest.web.services

import edu.ucdavis.fiehnlab.ctsrest.client.api.CtsService
import edu.ucdavis.fiehnlab.ctsrest.client.types.MoleculeResponse
import edu.ucdavis.fiehnlab.ctsrest.web.CtsProxy
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.TestContextManager

@SpringBootTest(value = Array("${fiehnlab.cts.config.name}"), classes = Array(classOf[CtsProxy]), webEnvironment = WebEnvironment.RANDOM_PORT)
class SmilesConversionServiceTest extends AnyWordSpec with Matchers {

  @Autowired
  val smilesConversionService: SmilesConversionService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "SmilesConversionService" should {

    "convert InChIKey to SMILES for ethanol (LFQSCWFLJHTTHZ-UHFFFAOYSA-N)" in {
      val smiles = smilesConversionService.inchikeyToSmiles("LFQSCWFLJHTTHZ-UHFFFAOYSA-N")
      smiles should not be empty
      println(s"Ethanol SMILES: $smiles")
    }

    "throw IllegalArgumentException for invalid InChIKey" in {
      an[Exception] should be thrownBy {
        smilesConversionService.inchikeyToSmiles("INVALID-INCHIKEY-N")
      }
    }
  }
}
