package edu.ucdavis.fiehnlab.ctsrest.web.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{CrossOrigin, RequestMapping}

@Controller
@CrossOrigin(origins = Array("*"))
class ViewController {
  /*
   * This is necessary because the Angular app is using HTML5 Mode
   * Routing is handled on the client side; the server simply needs to serve index.html
   */
  @RequestMapping(Array("/", "/batch", "/services"))
  def index(): String = {
    "forward:/index.html"
  }
}
