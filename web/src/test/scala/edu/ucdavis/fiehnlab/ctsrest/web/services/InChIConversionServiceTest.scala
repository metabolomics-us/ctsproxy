package edu.ucdavis.fiehnlab.ctsrest.web.services

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{Matchers, WordSpec}

class InChIConversionServiceTest extends WordSpec with Matchers with LazyLogging {

  "InChIConversionService" should {

    val inChIConversionService = new InChIConversionService

    "convert an inchi to smiles for ethanol" in {
      val inchi = "InChI=1S/C2H6O/c1-2-3/h3H,2H2,1H3"
      val result = inChIConversionService.inchiToSmiles(inchi)
      assert(result === "CCO")
    }

    "convert an inchi to smiles for L-ascorbic acid" in {
      val inchi = "InChI=1S/C6H8O6/c7-1-2(8)5-3(9)4(10)6(11)12-5/h2,5,7-8,10-11H,1H2/t2-,5+/m0/s1"
      val result = inChIConversionService.inchiToSmiles(inchi)
      assert(result === "C([C@@]([H])([C@]1([H])C(=O)C(=C(O)O1)O)O)O")
    }
  }
}
