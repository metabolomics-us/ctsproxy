package edu.ucdavis.fiehnlab.ctsrest.web

import edu.ucdavis.fiehnlab.ctsrest.casetojson.config.CaseClassToJSONSerializationAutoConfiguration
import edu.ucdavis.fiehnlab.ctsrest.client.api._
import edu.ucdavis.fiehnlab.ctsrest.client.core._
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration
import org.springframework.boot.{SpringApplication, WebApplicationType}
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.http.MediaType
import org.springframework.web.servlet.config.annotation.{ContentNegotiationConfigurer, CorsRegistry, WebMvcConfigurer}
import springfox.documentation.builders.{PathSelectors, RequestHandlerSelectors}
import springfox.documentation.service.{ApiInfo, Contact}
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

import java.util.Collections

/**
 * Created by diego on 2/15/2017.
 */

@EnableCaching
@SpringBootApplication
@EnableZuulProxy
class CtsProxy

object CtsProxy extends App {
  val app: SpringApplication = new SpringApplication(classOf[CtsProxy])
  app.setWebApplicationType(WebApplicationType.SERVLET)
  val context = app.run(args: _*)
}

@Configuration
@Import(Array(classOf[CaseClassToJSONSerializationAutoConfiguration], classOf[ServletWebServerFactoryAutoConfiguration]))
class CtsProxyConfig {

  @Bean
  def ctsClient: CtsService = new CtsClient()
}

@Configuration
class CtsCors extends WebMvcConfigurer {

  override def configureContentNegotiation(configurer: ContentNegotiationConfigurer): Unit = {
    configurer.favorPathExtension(false)
        .favorParameter(false)
        .parameterName("mediaType")
        .ignoreAcceptHeader(false)
        .useRegisteredExtensionsOnly(true)
        .defaultContentType(MediaType.APPLICATION_JSON)
        .mediaType("json", MediaType.APPLICATION_JSON)
  }

  override def addCorsMappings(registry: CorsRegistry): Unit = registry.addMapping("/**")
}

@Configuration
@EnableSwagger2
class SwaggerConfig {

  final val apiInfo = new ApiInfo(
    "Chemical Translation Service Proxy",
    "Connects the new web GUI to the old CTS API.",
    "2.6.15", "",
    new Contact("Diego Pedrosa", "https://cts.fiehnlab.ucdavis.edu", ""),
    "GPLv3",
    "https://www.gnu.org/licenses/gpl-3.0.en.html",
    Collections.emptyList()
  )


  @Bean
  def api: Docket = new Docket(DocumentationType.SWAGGER_2)
      .select
      .apis(RequestHandlerSelectors.basePackage("edu.ucdavis.fiehnlab.ctsrest"))
      .paths(PathSelectors.any)
      .build()
      .apiInfo(apiInfo)
}
