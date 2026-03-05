package edu.ucdavis.fiehnlab.ctsrest.client.core

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ctsrest.client.api.CtsService
import edu.ucdavis.fiehnlab.ctsrest.client.types._
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.http.{HttpEntity, HttpHeaders, MediaType}
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Component
class CtsClient extends CtsService with LazyLogging {

  @Value("${cts.old.url:https://oldcts.fiehnlab.ucdavis.edu/service}")
  val baseUrl = ""

  @Autowired
  val restTemplate: RestTemplate = null

  def convert(from: String, to: String, searchTerm: String): Seq[ConversionResult] = {
    val response = restTemplate.getForObject[Seq[Map[String, Any]]](
      baseUrl + "/convert/{from}/{to}/{searchTerm}",
      classOf[Seq[Map[String, Any]]],
      from, to, searchTerm
    ).map(item => ConversionResult(
      item.getOrElse("fromIdentifier", "undefined").toString,
      item("toIdentifier").toString,
      item("searchTerm").toString,
      item("result").asInstanceOf[Seq[String]])
    )
    response
  }

  def score(from: String, value: String, algorithm: String): ScoreResult = {
    restTemplate.getForObject[ScoreResult](baseUrl + "/score/{from}/{value}/{algorithm}", classOf[ScoreResult], from, value, algorithm)
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

  private def formEntity(params: (String, String)*): HttpEntity[LinkedMultiValueMap[String, String]] = {
    val headers = new HttpHeaders()
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED)
    val body = new LinkedMultiValueMap[String, String]()
    params.foreach { case (k, v) => body.add(k, v) }
    new HttpEntity(body, headers)
  }

  def inchi2Mol(inchiCode: String): MoleculeResponse = {
    val response = restTemplate.postForObject[MoleculeResponse](s"${baseUrl}/inchitomol",
      formEntity("inchicode" -> inchiCode), classOf[MoleculeResponse])

    logger.info(s"RESPONSE: ${response}")
    response
  }

  def mol2Inchi(mol: String): InChIPairResponse = {
    // Add MOL header since the old CTS doesn't recognize it as valid otherwise
    val molValue = mol.head match {
      case '\n' => "MOL" + mol
      case _ => mol
    }

    val response = restTemplate.postForObject[InChIPairResponse](s"${baseUrl}/moltoinchi",
      formEntity("mol" -> molValue), classOf[InChIPairResponse])

    logger.info(s"RESPONSE: ${response}")
    response
  }

  def smiles2Inchi(smilesCode: String): InChIResponse = {
    val response = restTemplate.postForObject[InChIResponse](s"${baseUrl}/smilestoinchi",
      formEntity("smiles" -> smilesCode), classOf[InChIResponse])

    logger.info(s"RESPONSE: ${response}")
    response
  }

  def inchiCode2InchiKey(inchiCode: String): InChIPairResponse = {
    val response = restTemplate.postForObject[InChIPairResponse](s"${baseUrl}/inchicodetoinchikey",
      formEntity("inchicode" -> inchiCode), classOf[InChIPairResponse])

    logger.info(s"RESPONSE: ${response}")
    response
  }
}
