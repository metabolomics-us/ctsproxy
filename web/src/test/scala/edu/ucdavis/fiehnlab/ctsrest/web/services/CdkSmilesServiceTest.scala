package edu.ucdavis.fiehnlab.ctsrest.web.services

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CdkSmilesServiceTest extends AnyWordSpec with Matchers {

  val service = new CdkSmilesService()

  // Ethanol MOL block
  val ethanolMol: String =
    """
      |  Mrv2211 02092306282D
      |
      |  3  2  0  0  0  0            999 V2000
      |    0.0000    0.8250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
      |    0.0000    0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
      |    0.7145   -0.4125    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0
      |  1  2  1  0  0  0  0
      |  2  3  1  0  0  0  0
      |M  END
      |""".stripMargin

  "CdkSmilesService" should {

    "convert ethanol MOL block to SMILES" in {
      val smiles = service.molToSmiles(ethanolMol)
      smiles should not be empty
      smiles should be("OCC")
    }

    "throw exception for invalid MOL block" in {
      an[Exception] should be thrownBy {
        service.molToSmiles("not a valid mol block")
      }
    }
  }
}
