package edu.ucdavis.fiehnlab.ctsrest.web.controllers

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ctsrest.client.api.CtsService
import edu.ucdavis.fiehnlab.ctsrest.client.types._
import edu.ucdavis.fiehnlab.ctsrest.web.services.SmilesConversionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation._
import org.springframework.web.server.ResponseStatusException



/**
  * Created by diego on 2/15/2017.
  */
@RestController
@CrossOrigin(origins = Array("*"))
@RequestMapping(path = Array("/rest"))
class CtsController extends LazyLogging {

  @Autowired
  val client: CtsService = null

  @Autowired
  val smilesConversionService: SmilesConversionService = null

  @Cacheable(Array("simple_convert"))
  @GetMapping(path = Array("/convert/{from}/{to}/{searchTerm}"))
  def convertSimple(@PathVariable from: String, @PathVariable to: String, @PathVariable searchTerm: String): Seq[ConversionResult] = {
    if (from.equalsIgnoreCase("SMILES") || from.equalsIgnoreCase("ChemSpider")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, s"Conversion from '$from' is not supported")
    }
    if (to.equalsIgnoreCase("ChemSpider")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, s"Conversion to '$to' is not supported")
    }
    if (to.equalsIgnoreCase("SMILES")) {
      convertToSmiles(from, searchTerm)
    } else {
      client.convert(from, to, searchTerm)
    }
  }

  private def convertToSmiles(from: String, searchTerm: String): Seq[ConversionResult] = {
    try {
      if (from.equalsIgnoreCase("InChIKey")) {
        val smiles = smilesConversionService.inchikeyToSmiles(searchTerm)
        Seq(ConversionResult("InChIKey", "SMILES", searchTerm, Seq(smiles)))
      } else {
        // Chain: from -> InChIKey -> MOL -> SMILES
        val inchikeyResults = client.convert(from, "InChIKey", searchTerm)
        val smilesResults = inchikeyResults.flatMap { result =>
          result.results.flatMap { inchikey =>
            try {
              Some(smilesConversionService.inchikeyToSmiles(inchikey))
            } catch {
              case e: Exception =>
                logger.warn(s"CDK conversion failed for InChIKey $inchikey: ${e.getMessage}")
                None
            }
          }
        }.distinct

        if (smilesResults.nonEmpty) {
          Seq(ConversionResult(from, "SMILES", searchTerm, smilesResults))
        } else {
          // Fall back to old CTS API
          client.convert(from, "SMILES", searchTerm)
        }
      }
    } catch {
      case e: Exception =>
        logger.warn(s"CDK SMILES conversion failed, falling back to CTS API: ${e.getMessage}")
        client.convert(from, "SMILES", searchTerm)
    }
  }

  @Cacheable(Array("inchikey_to_smiles"))
  @GetMapping(path = Array("/inchikeytosmiles/{inchikey}"))
  def inchikeyToSmiles(@PathVariable("inchikey") inchikey: String): ConversionResult = {
    val smiles = smilesConversionService.inchikeyToSmiles(inchikey)
    ConversionResult("InChIKey", "SMILES", inchikey, Seq(smiles))
  }

  @Cacheable(Array("expand_formula"))
  @GetMapping(path = Array("/expandformula/{formula}"))
  def expandFormula(@PathVariable formula: String): FormulaResponse = {
    client.expandFormula(formula)
  }

  @Cacheable(Array("from_values"))
  @GetMapping(path = Array("/fromValues"))
  def fromValues: Seq[String] = {
    client.sourceIdNames().filterNot(v => v.equalsIgnoreCase("Chemspider") || v.equalsIgnoreCase("SMILES"))
  }

  @Cacheable(Array("to_values"))
  @GetMapping(path = Array("/toValues"))
  def toValues: Seq[String] = {
    client.targetIdNames().filterNot(_.equalsIgnoreCase("Chemspider"))
  }

  @Cacheable(Array("bio_count"))
  @GetMapping(path = Array("/countBiological/{inchikey}"))
  def compoundBiologicalCount(@PathVariable("inchikey") inchikey: String): Map[String, Int] = {
    client.compoundBiologicalCount(inchikey)
  }

  @Cacheable(Array("scoring"))
  @GetMapping(path = Array("/score/{from}/{value}/{algorithm}"))
  def score(@PathVariable("from") from: String, @PathVariable("value") to: String, @PathVariable("algorithm") algorithm: String): ScoreResult = {
    client.score(from, to, algorithm)
  }

  @Cacheable(Array("inchikey_mol"))
  @GetMapping(path = Array("/inchikeytomol/{inchikey}"))
  def inchiKey2Mol(@PathVariable("inchikey") inchikey: String): MoleculeResponse = {
    client.inchiKey2Mol(inchikey)
  }

  @Cacheable(Array("cmpd_synonyms"))
  @GetMapping(path = Array("/synonyms/{inchikey}"), produces = Array("application/json"))
  def compoundSynonyms(@PathVariable("inchikey") inchikey: String): Seq[String] = {
    client.compoundSynonyms(inchikey)
  }

  //    @Cacheable(Array("extidCount"))
  //    @GetMapping(path = Array("/extidCount/{extidName}")
  //    def ExtidScoreResponse extidCount(@PathVariable("extidName") String extidName) {
  //	    client.getExtidCount(extidName)
  //    }


  /** ***** POST REQUESTS ********/

  @Cacheable(Array("mol2inchi"))
  @PostMapping(path = Array("/moltoinchi"), consumes = Array("application/json"))
  def mol2Inchi(mol: String): InChIPairResponse = {
    client.mol2Inchi(mol)
  }

  @Cacheable(Array("smiles2inchi"))
  @PostMapping(path = Array("/smilestoinchi"), consumes = Array("application/json"))
  def smiles2Inchi(smilesCode: String): InChIResponse = {
    client.smiles2Inchi(smilesCode)
  }

  @Cacheable(Array("inchi2inchikey"))
  @PostMapping(path = Array("/inchicodetoinchikey"), consumes = Array("application/json"))
  def inchiCode2InchiKey(inchi: String): InChIPairResponse = {
    client.inchiCode2InchiKey(inchi)
  }

  @Cacheable(Array("inchi2mol"))
  @PostMapping(path = Array("/inchicodetomol"), consumes = Array("application/json"))
  def inchiCode2Mol(inchi: String): MoleculeResponse = {
    client.inchi2Mol(inchi)
  }
}
