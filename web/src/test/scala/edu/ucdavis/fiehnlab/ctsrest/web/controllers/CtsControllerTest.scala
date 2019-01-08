package edu.ucdavis.fiehnlab.ctsrest.web.controllers

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ctsrest.client.types.FormulaResponse
import edu.ucdavis.fiehnlab.ctsrest.web.{CtsProxy, CtsProxyConfig}
import edu.ucdavis.fiehnlab.wcmc.utilities.casetojson.config.CaseClassToJSONSerializationAutoConfiguration
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.collection.JavaConverters._

/**
  * Created by diego on 2/15/2017.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[CtsProxy]), webEnvironment = WebEnvironment.RANDOM_PORT)
class CtsControllerTest extends WordSpec with Matchers with LazyLogging {

  @Autowired
  val restTemplate: TestRestTemplate = null

  @LocalServerPort
  val port: Int = 0

  new TestContextManager(this.getClass).prepareTestInstance(this)

  val baseUrl = s"http://localhost:${port}/rest"

  "CtsController" should {

    "fromValues" in {
      val response = restTemplate.getForObject[Seq[String]](baseUrl + "/fromValues", classOf[Seq[String]])
      response should not be empty
      response.size should be > 10
      response should contain allOf("Chemical Name", "Human Metabolome Database", "InChIKey", "KEGG")
    }

    "toValues" in {
      val response = restTemplate.getForObject[Seq[String]](baseUrl + "/toValues", classOf[Seq[String]])
      response should not be empty
      response.size should be > 10
      response should contain allOf("Chemical Name", "Human Metabolome Database", "InChI Code", "InChIKey", "KEGG")
    }

    "getFormulaExpansion" in {
      val response = restTemplate.getForObject(baseUrl + "/expandformula/C2H6O", classOf[FormulaResponse])
      response shouldEqual FormulaResponse("C2H6O", "CCHHHHHHO", null)
    }

    "getSimpleConversion" in {
      val response = restTemplate.getForObject[Seq[Map[String, Any]]](baseUrl + "/convert/Chemical Name/InChIKey/alanine",
        classOf[Seq[Map[String, Any]]])
      println(response)

      response should not be empty
      response.head.getOrElse("fromIdentifier", "") should be ("Chemical Name")
      response.head.getOrElse("toIdentifier", "") should be ("InChIKey")
      response.head.getOrElse("searchTerm", "") should be ("alanine")
      response.head.getOrElse("results", Seq.empty) shouldBe a [Seq[_]]

      response.head.getOrElse("results", Seq.empty) should contain
          allOf("QNAYBMKLOCPYGJ-REOHCLBHSA-N", "QNAYBMKLOCPYGJ-UHFFFAOYSA-N", "QNAYBMKLOCPYGJ-UWTATZPHSA-N")
    }

    "testSecondCallHitsCache" ignore {
      // no cache header set, so test is ignored for now
      val response = restTemplate.getForEntity[Seq[String]](baseUrl + "/fromValues", classOf[Seq[String]])
      println(response.getHeaders.asScala.mkString("\n"))
      response.getHeaders.get("X-Proxy-Cache") shouldBe "MISS"

      val response2 = restTemplate.getForEntity[Seq[String]](baseUrl + "/fromValues", classOf[Seq[String]])
      response.getHeaders.get("X-Proxy-Cache") shouldBe "HIT"
    }
  }
}
