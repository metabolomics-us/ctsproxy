package edu.ucdavis.fiehnlab.ctsrest.web

import edu.ucdavis.fiehnlab.wcmc.utilities.casetojson.config.CaseClassToJSONSerializationAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.{SpringApplication, WebApplicationType}
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.http.MediaType
import org.springframework.web.servlet.config.annotation.{ContentNegotiationConfigurer, CorsRegistry, WebMvcConfigurer}
import springfox.documentation.builders.{PathSelectors, RequestHandlerSelectors}
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import edu.ucdavis.fiehnlab.ctsrest.client.api._
import edu.ucdavis.fiehnlab.ctsrest.client.core._
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration

/**
  * Created by diego on 2/15/2017.
  */

@EnableCaching
@SpringBootApplication(scanBasePackageClasses = Array(classOf[CtsProxyConfig], classOf[CaseClassToJSONSerializationAutoConfiguration], classOf[ServletWebServerFactoryAutoConfiguration]))
class CtsProxy

object CtsProxy extends App {
  val app: SpringApplication = new SpringApplication(classOf[CtsProxy])
  app.setWebApplicationType(WebApplicationType.SERVLET)
  val context = app.run(args: _*)
}

@Configuration
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
    "Diego Pedrosa",
    "GPLv3",
    "https://www.gnu.org/licenses/gpl-3.0.en.html"
  )


  @Bean
  def api: Docket = new Docket(DocumentationType.SWAGGER_2)
      .select
      .apis(RequestHandlerSelectors.basePackage("edu.ucdavis.fiehnlab.ctsRest"))
      .paths(PathSelectors.any)
      .build()
      .apiInfo(apiInfo)
}
