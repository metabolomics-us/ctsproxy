package edu.ucdavis.fiehnlab.ctsrest.casetojson.config

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{DeserializationContext, DeserializationFeature, JsonDeserializer, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.scala.Logging
import org.springframework.boot.autoconfigure._
import org.springframework.context.annotation.{Bean, Configuration, Primary}
import org.springframework.core.{Ordered => SpringOrdered}
import org.springframework.http.HttpRequest
import org.springframework.http.client._
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.util.CollectionUtils
import org.springframework.web.client.{DefaultResponseErrorHandler, ResponseErrorHandler, RestClientException, RestTemplate}

import java.io.{BufferedReader, IOException, InputStreamReader}
import java.nio.charset.StandardCharsets
import java.util
import java.util.stream.Collectors

/**
 * Created by wohlgemuth on 7/11/17.
 */
@Configuration
@AutoConfigureOrder(SpringOrdered.HIGHEST_PRECEDENCE)
class CaseClassToJSONSerializationAutoConfiguration extends Logging {

  @Bean
  def objectMapper: ObjectMapper = {

    logger.info("creating custom object mapper...")
    val mapper = JsonMapper.builder().addModule(DefaultScalaModule).addModule(new JavaTimeModule()).build()

    val simpleModule = new SimpleModule()
    simpleModule.addDeserializer(classOf[Double], new ForceDoubleDeserializer)

    // mapper.registerModule(simpleModule)

    //required, in case we are provided with a list of value
    mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false)
    mapper.setSerializationInclusion(Include.NON_NULL)
    mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)

    mapper
  }

  @Primary
  @Bean
  def restTemplate(mappingJackson2HttpMessageConverter: MappingJackson2HttpMessageConverter): RestTemplate = {
    logger.info("creating custom template with Jackson for scala support")
    val factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
    val rest: RestTemplate = new RestTemplate(factory)
    rest.getMessageConverters.add(0, mappingJackson2HttpMessageConverter)
    rest.setErrorHandler(new ResponseErrorHandler {

      private val errorHandler = new DefaultResponseErrorHandler

      override def hasError(clientHttpResponse: ClientHttpResponse): Boolean = errorHandler.hasError(clientHttpResponse)

      override def handleError(clientHttpResponse: ClientHttpResponse): Unit = {
        throw new RestClientException(s"status code: ${clientHttpResponse.getStatusCode}, status text: ${clientHttpResponse.getStatusText}, body: ${IOUtils.toString(clientHttpResponse.getBody(), "utf-8")}")
      }
    })

    val interceptors = if (CollectionUtils.isEmpty(rest.getInterceptors)) new util.ArrayList[ClientHttpRequestInterceptor]() else rest.getInterceptors
    interceptors.add(new LoggingInterceptor)
    rest.setInterceptors(interceptors)
    rest
  }


  @Bean
  def mappingJacksonHttpMessageConverter(objectMapper: ObjectMapper): MappingJackson2HttpMessageConverter = {
    new MappingJackson2HttpMessageConverter(objectMapper)
  }

}


class LoggingInterceptor extends ClientHttpRequestInterceptor with Logging {
  @throws[IOException]
  override def intercept(req: HttpRequest, reqBody: Array[Byte], ex: ClientHttpRequestExecution): ClientHttpResponse = {
    logger.debug(s"Request body: ${new String(reqBody, StandardCharsets.UTF_8)}")
    val response = ex.execute(req, reqBody)
    val isr = new InputStreamReader(response.getBody, StandardCharsets.UTF_8)
    val body = new BufferedReader(isr).lines.collect(Collectors.joining("\n"))
    logger.debug(s"Response body:${body}")
    response
  }


}

class ForceDoubleDeserializer extends JsonDeserializer[Double] {
  @throws[IOException]
  override def deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Double = {
    try {
      val result = jsonParser.getValueAsDouble
      result
    }
    catch {
      case x: Exception =>
        throw x
    }
  }
}
