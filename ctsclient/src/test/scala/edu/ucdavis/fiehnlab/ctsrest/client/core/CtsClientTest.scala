package edu.ucdavis.fiehnlab.ctsrest.client.core

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ctsrest.client.types.{InChIPairResponse, MoleculeResponse, ScoredInchi}
import edu.ucdavis.fiehnlab.ctsrest.casetojson.config.CaseClassToJSONSerializationAutoConfiguration
import org.junit.runner.RunWith
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner


@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[CtsClientTestConfig], classOf[CaseClassToJSONSerializationAutoConfiguration]))
class CtsClientTest extends AnyWordSpec with Matchers with LazyLogging {

  @Autowired
  val client: CtsClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "CtsClientTest" should {
    val mol = MoleculeResponse("\n  CDK     1005182215\n\n  9  8  0  0  0  0  0  0  0  0999 V2000\n    1.2990   -0.7500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    2.5981   -0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    3.8971   -0.7500    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n    0.0000    0.0000    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n    0.3349   -1.8991    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n    2.2632   -1.8991    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n    3.5623    1.1491    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n    1.6339    1.1491    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n    5.1962   -0.0000    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n  2  1  1  0  0  0  0 \n  3  2  1  0  0  0  0 \n  1  4  1  0  0  0  0 \n  1  5  1  0  0  0  0 \n  1  6  1  0  0  0  0 \n  2  7  1  0  0  0  0 \n  2  8  1  0  0  0  0 \n  3  9  1  0  0  0  0 \nM  END\n", null)

    "have a client" in {
      client != null
    }

    "list source names" in {
      val sources = client.sourceIdNames()
      sources should not be empty
      sources should contain allOf ("PubChem CID","InChIKey","Chemical Name")
    }

    "list target names" in {
      val targets = client.targetIdNames()
      targets should not be empty
      targets should contain allOf ("PubChem CID","InChIKey","Chemical Name", "InChI Code")
    }

    "expandFormula" in {
      client.expandFormula("H2O") should have (
        'formula ("H2O"),
        'result ("HHO")
      )

      client.expandFormula("C2H6O") should have (
        'formula ("C2H6O"),
        'result ("CCHHHHHHO")
      )
    }

    "convert" in {
      val response = client.convert("chemical name", "inchikey", "alanine")

      response.head.fromIdentifier === "chemical name"
      response.head.searchTerm === "alanine"
      response.head.toIdentifier === "inchikey"
      response.head.results should contain allOf ("QNAYBMKLOCPYGJ-REOHCLBHSA-N", "QNAYBMKLOCPYGJ-UHFFFAOYSA-N", "QNAYBMKLOCPYGJ-UWTATZPHSA-N")
    }

    "score" in {
      val response = client.score("chemical name", "alanine", "biological")

      response.from should equal("chemical name")
      response.searchTerm should equal("alanine")
      response.result.head should equal(ScoredInchi("QNAYBMKLOCPYGJ-REOHCLBHSA-N", 1))
    }

    "compoundBiologicalCount" in {
      val response = client.compoundBiologicalCount("QNAYBMKLOCPYGJ-REOHCLBHSA-N")
      response should contain allOf ("KEGG" -> 2, "BioCyc" -> 1, "Human Metabolome Database" -> 1, "total" -> 4)
    }

    "compoundExtidCount" in {
      val response = client.compoundExtidCount("QNAYBMKLOCPYGJ-REOHCLBHSA-N")
      response.datasource_count === 277
    }

    "compoundSynonyms" in {
      val response = client.compoundSynonyms("QNAYBMKLOCPYGJ-REOHCLBHSA-N")
      response should contain allOf ("L-alanine", "Alanine", "(S)-2-amino-Propanoate")
    }

    "compoundInfo" in {
      val response = client.compoundInfo("QNAYBMKLOCPYGJ-REOHCLBHSA-N")

      response.inchikey === "QNAYBMKLOCPYGJ-REOHCLBHSA-N"
      response.inchicode === "InChI=1S/C3H7NO2/c1-2(4)3(5)6/h2H,4H2,1H3,(H,5,6)/t2-/m0/s1"
      response.exactmass === 89.09331
      response.molweight === 89.04768
      response.formula === "C3H7NO2"
    }

    "inchiKey2Mol" in {
      val response = client.inchiKey2Mol("LFQSCWFLJHTTHZ-UHFFFAOYSA-N")

      response.molecule.split("\n").filterNot(_.contains("  CDK  ")) should equal(mol.molecule.split("\n").filterNot(_.contains("  CDK  ")))
    }

    "inchi2Mol" in {
      var response = client.inchi2Mol("InChI=1S/C2H6O/c1-2-3/h3H,2H2,1H3")
      logger.info(s"RESPONSE: ${response}")

      // Drop header rows as CDK generated unique identifiers per conversion
      response.molecule.split('\n').drop(2) shouldEqual mol.molecule.split('\n').drop(2)
    }

    "mol2Inchi" in {
      val response = client.mol2Inchi(mol.molecule)
      logger.info(s"RESPONSE: ${response}")
      response shouldEqual InChIPairResponse("LFQSCWFLJHTTHZ-UHFFFAOYSA-N", "InChI=1S/C2H6O/c1-2-3/h3H,2H2,1H3")
    }

    "smiles2Inchi" in {
      val response = client.smiles2Inchi("CCO")
      logger.info(s"RESPONSE: ${response}")
      response.inchicode shouldEqual "InChI=1S/C2H6O/c1-2-3/h3H,2H2,1H3"
    }

    "inchiCode2InchiKey" in {
      val response = client.inchiCode2InchiKey("InChI=1S/C2H6O/c1-2-3/h3H,2H2,1H3")
      logger.info(s"RESPONSE: ${response}")
      response shouldEqual InChIPairResponse("LFQSCWFLJHTTHZ-UHFFFAOYSA-N", "InChI=1S/C2H6O/c1-2-3/h3H,2H2,1H3")
    }
  }
}

@Configuration
class CtsClientTestConfig {
  @Bean
  def client: CtsClient = new CtsClient
}
