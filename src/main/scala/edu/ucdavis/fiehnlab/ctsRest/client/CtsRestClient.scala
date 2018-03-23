package edu.ucdavis.fiehnlab.ctsRest.client

import com.typesafe.scalalogging.LazyLogging
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.http.{HttpEntity, HttpHeaders, MediaType}
import org.springframework.stereotype.Service
import org.springframework.util.{LinkedMultiValueMap, MultiValueMap}
import org.springframework.web.client.RestTemplate
import scala.collection.JavaConverters._
/**
  * Created by diego on 3/22/2018
  **/
@Service
class CtsRestClient() extends LazyLogging {
  @Autowired
  val template: RestTemplate = null

  @Value("${fiehnlab.cts.config.url:http://oldcts.fiehnlab.ucdavid.edu}")
  private val baseUrl = ""

  /**
    * converts a molecule definition (in MDL format) to inchiKey and inchi code
    * @param mol
    * @return
    */
  def mol2Inchi (mol: String): String = {
    "inchi"
  }

  /**
    * Converts an inchi code to a molecule definition in MDL format
    * @param inchicode
    * @return
    */
  def inchi2Mol(inchicode: String): String = {
    "/service/inchitomol"
  }

  def smiles2inchi(inchicode: String): String = {
    "/service/smilestoinchi"
  }

  def inchiCode2InchiKey(inchicode: String): String = {
    logger.debug(s"Inchi Code: $inchicode")
    val resp = template.postForEntity(s"$baseUrl/service/inchicodetoinchikey", prepareRequest(Map("inchicode"->inchicode)), classOf[String])
    resp.toString
  }

  private def prepareRequest(params: Map[String, String]): HttpEntity[AnyRef] = {
    val headers: HttpHeaders = new HttpHeaders()
    headers.setAll(Map("Content-Type"->"application/json","Accepts"->"application/json").asJava)

    new HttpEntity(params.asJava,headers)
  }
}
