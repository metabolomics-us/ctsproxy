package edu.ucdavis.fiehnlab.ctsrest.client.core

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ctsrest.client.api.CtsService
import edu.ucdavis.fiehnlab.ctsrest.client.types._
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class CtsClient extends CtsService with LazyLogging {

  @Value("${cts.old.url:https://oldcts.fiehnlab.ucdavis.edu/service}")
  val baseUrl = ""

  @Autowired
  val restTemplate: RestTemplate = null

  def convert(from: String, to: String, searchTerm: String): Seq[ConversionResult] = {
    val response = restTemplate.getForObject[Seq[Map[String, Any]]](
      baseUrl + s"/convert/${from}/${to}/${searchTerm}",
      classOf[Seq[Map[String, Any]]]
    ).map(item => ConversionResult(
      item.getOrElse("fromIdentifier", "undefined").toString,
      item("toIdentifier").toString,
      item("searchTerm").toString,
      item("result").asInstanceOf[Seq[String]])
    )
    response
  }

  def score(from: String, value: String, algorithm: String): ScoreResult = {
    restTemplate.getForObject[ScoreResult](s"${baseUrl}/score/${from}/${value}/${algorithm}", classOf[ScoreResult])
  }

  def inchiKey2Mol(inchikey: String): MoleculeResponse = {
    restTemplate.getForObject[MoleculeResponse](s"${baseUrl}/inchikeytomol/${inchikey}", classOf[MoleculeResponse])
  }

  def expandFormula(formula: String): FormulaResponse = {
    restTemplate.getForObject[FormulaResponse](s"${baseUrl}/expandFormula/${formula}", classOf[FormulaResponse])
  }

  def compoundInfo(inchikey: String): CompoundResponse = {
    restTemplate.getForObject[CompoundResponse](s"${baseUrl}/compound/${inchikey}", classOf[CompoundResponse])
  }

  def compoundSynonyms(inchikey: String): Seq[String] = {
    restTemplate.getForObject[Seq[String]](s"${baseUrl}/synonyms/${inchikey}", classOf[Seq[String]])
  }

  def compoundExtidCount(inchikey: String): ExtidCountResponse = {
    restTemplate.getForObject[ExtidCountResponse](s"${baseUrl}/count/${inchikey}", classOf[ExtidCountResponse])
  }

  def compoundBiologicalCount(inchikey: String): Map[String, Int] = {
    restTemplate.getForObject[Map[String, Int]](s"${baseUrl}/countBiological/${inchikey}", classOf[Map[String, Int]])
  }

  def sourceIdNames(): Seq[String] = {
    restTemplate.getForObject[Seq[String]](s"${baseUrl}/conversion/fromValues", classOf[Seq[String]])
  }

  def targetIdNames(): Seq[String] = {
    restTemplate.getForObject[Seq[String]](s"${baseUrl}/conversion/toValues", classOf[Seq[String]])
  }

  def chemifyQuery(name: String): String = {
    restTemplate.getForObject[String](s"${baseUrl}/chemify/rest/identify/$name", classOf[String])
  }

  /* ---------------------------POST REQUESTS-------------------------------*/

  def inchi2Mol(inchiCode: String): MoleculeResponse = {
    val entity = new HttpEntity(InChIConversionRequest(inchiCode))

    val response = restTemplate.postForObject[MoleculeResponse](s"${baseUrl}/inchitomol",
      entity, classOf[MoleculeResponse])

    logger.info(s"RESPONSE: ${response}")
    response
  }

  def mol2Inchi(mol: String): InChIPairResponse = {
    val entity = new HttpEntity(MOLConversionRequest(mol))

    val response = restTemplate.postForObject[InChIPairResponse](s"${baseUrl}/moltoinchi",
      entity, classOf[InChIPairResponse])

    logger.info(s"RESPONSE: ${response}")
    response
  }

  def smiles2Inchi(smilesCode: String): InChIResponse = {
    val entity = new HttpEntity(SMILESConversionRequest(smilesCode))

    val response = restTemplate.postForObject[InChIResponse](s"${baseUrl}/smilestoinchi",
      entity, classOf[InChIResponse])

    logger.info(s"RESPONSE: ${response}")
    response
  }

  def inchiCode2InchiKey(inchiCode: String): InChIPairResponse = {
    val entity = new HttpEntity(InChIConversionRequest(inchiCode))

    val response = restTemplate.postForObject[InChIPairResponse](s"${baseUrl}/inchicodetoinchikey",
      entity, classOf[InChIPairResponse])

    logger.info(s"RESPONSE: ${response}")
    response
  }
}
