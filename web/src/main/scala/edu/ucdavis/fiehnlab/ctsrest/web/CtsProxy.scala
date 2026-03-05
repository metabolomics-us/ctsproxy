package edu.ucdavis.fiehnlab.ctsrest.web

import edu.ucdavis.fiehnlab.ctsrest.casetojson.config.CaseClassToJSONSerializationAutoConfiguration
import edu.ucdavis.fiehnlab.ctsrest.client.api._
import edu.ucdavis.fiehnlab.ctsrest.client.core._
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.{Contact, Info, License}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration
import org.springframework.boot.{SpringApplication, WebApplicationType}
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.http.MediaType
import org.springframework.web.servlet.config.annotation.{ContentNegotiationConfigurer, CorsRegistry, WebMvcConfigurer}

/**
 * Created by diego on 2/15/2017.
 */

@EnableCaching
@SpringBootApplication
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
    configurer
        .ignoreAcceptHeader(false)
        .defaultContentType(MediaType.APPLICATION_JSON)
        .mediaType("json", MediaType.APPLICATION_JSON)
  }

  override def addCorsMappings(registry: CorsRegistry): Unit = registry.addMapping("/**")
}

@Configuration
class OpenApiConfig {

  @Bean
  def customOpenAPI: OpenAPI = {
    new OpenAPI()
        .info(new Info()
            .title("Chemical Translation Service Proxy")
            .description("Connects the new web GUI to the old CTS API.")
            .version("2.6.15")
            .contact(new Contact()
                .name("Diego Pedrosa")
                .url("https://cts.fiehnlab.ucdavis.edu"))
            .license(new License()
                .name("GPLv3")
                .url("https://www.gnu.org/licenses/gpl-3.0.en.html")))
  }
}
