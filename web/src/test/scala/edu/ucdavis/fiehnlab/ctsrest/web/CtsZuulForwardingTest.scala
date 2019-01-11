package edu.ucdavis.fiehnlab.ctsrest.web

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ctsrest.client.types.FormulaResponse
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.TestRestTemplate.HttpClientOption
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.collection.JavaConverters._

/**
  * Created by sajjan on 1/11/2019.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(value = Array("${fiehnlab.cts.config.name}"), classes = Array(classOf[CtsProxy]), webEnvironment = WebEnvironment.RANDOM_PORT)
class CtsZuulForwardingTest extends WordSpec with Matchers with LazyLogging {

  @Autowired
  val restTemplate: TestRestTemplate = null

  @LocalServerPort
  val port: Int = 0

  new TestContextManager(this.getClass).prepareTestInstance(this)

  val baseUrl = s"http://localhost:${port}/service"

  "CtsZuulForwarding" should {

    "return compound properties" in {
      val response = restTemplate.getForObject[Map[String, Any]](baseUrl + "/compound/MSPCIZMDDUQPGJ-UHFFFAOYSA-N", classOf[Map[String, Any]])

      response.getOrElse("inchikey", "") should be ("MSPCIZMDDUQPGJ-UHFFFAOYSA-N")
      response.getOrElse("inchicode", "") should be ("InChI=1S/C6H12F3NOSi/c1-10(12(2,3)4)5(11)6(7,8)9/h1-4H3")
    }

    "convert InChIKey to KEGG" in {
      val response = restTemplate.getForObject[Seq[Map[String, Any]]](baseUrl + "/convert/InChIKey/KEGG/MSPCIZMDDUQPGJ-UHFFFAOYSA-N", classOf[Seq[Map[String, Any]]])

      response should not be empty
      response.size == 1
      response.head.getOrElse("fromIdentifier", "") should be ("InChIKey")
      response.head.getOrElse("searchTerm", "") should be ("MSPCIZMDDUQPGJ-UHFFFAOYSA-N")
      response.head.getOrElse("toIdentifier", "") should be ("KEGG")
    }
  }
}
