package edu.ucdavis.fiehnlab.ctsrest.web.controllers

import com.typesafe.scalalogging.LazyLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http._
import org.springframework.web.bind.annotation.{RequestMapping, RestController}
import org.springframework.web.client.{HttpStatusCodeException, RestTemplate}

import java.net.URI

// Simple reverse proxy replacing Netflix Zuul.
// Forwards /service/ requests to the configured CTS backend.
@RestController
class ProxyController extends LazyLogging {

  @Value("${cts.proxy.url:http://cts}")
  val proxyTargetUrl: String = ""

  @org.springframework.beans.factory.annotation.Autowired
  val restTemplate: RestTemplate = null

  @RequestMapping(Array("/service/**"))
  def proxy(request: HttpServletRequest): ResponseEntity[Array[Byte]] = {
    val path = request.getRequestURI
    val query = Option(request.getQueryString).map("?" + _).getOrElse("")
    val targetUrl = s"$proxyTargetUrl$path$query"

    logger.debug(s"Proxying ${request.getMethod} $path -> $targetUrl")

    val method = HttpMethod.valueOf(request.getMethod)

    val headers = new HttpHeaders()
    val headerNames = request.getHeaderNames
    while (headerNames.hasMoreElements) {
      val name = headerNames.nextElement()
      val values = request.getHeaders(name)
      while (values.hasMoreElements) {
        headers.add(name, values.nextElement())
      }
    }
    headers.remove(HttpHeaders.HOST)

    val body = request.getInputStream.readAllBytes()
    val entity = new HttpEntity[Array[Byte]](if (body.nonEmpty) body else null, headers)

    try {
      val response = restTemplate.exchange(
        new URI(targetUrl),
        method,
        entity,
        classOf[Array[Byte]]
      )

      val responseHeaders = new HttpHeaders()
      responseHeaders.putAll(response.getHeaders)
      responseHeaders.remove(HttpHeaders.TRANSFER_ENCODING)

      new ResponseEntity[Array[Byte]](response.getBody, responseHeaders, response.getStatusCode)
    } catch {
      case ex: HttpStatusCodeException =>
        new ResponseEntity[Array[Byte]](
          ex.getResponseBodyAsByteArray,
          ex.getResponseHeaders,
          ex.getStatusCode
        )
    }
  }
}
