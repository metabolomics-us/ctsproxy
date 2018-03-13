package edu.ucdavis.fiehnlab.ctsRest.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ViewController {
    /*
    * This is necessary because the Angular app is using HTML5 Mode
    * Routing is handled on the client side; the server simply needs to serve index.html
    *
    * */
    @GetMapping(value = {
            "/",
            "/batch",
            "/services"
    }, produces = "text/html")
    public String index() {
        return "forward:/index.html";
    }
}
