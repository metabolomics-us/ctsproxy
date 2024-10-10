package edu.ucdavis.fiehnlab.ctsrest.casetojson.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

/**
 * Created by wohlgemuth on 10/11/17.
 */
@SpringBootTest
class CaseClassToJSONSerializationAutoConfigurationTest extends AnyFunSuite {

  @Autowired
  val objectMapper: ObjectMapper = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  test("testObjectMapper") {
    assert(objectMapper != null)

    val out = new ByteArrayOutputStream()
    objectMapper.writeValue(out, TestToSerialize("tada"))

    val in = objectMapper.readValue(new ByteArrayInputStream(out.toByteArray), classOf[TestToSerialize])

    assert(in.name == "tada")
  }

}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class CaseClassToJSONSerializationAutoConfigurationTestConfig

case class TestToSerialize(name: String)
