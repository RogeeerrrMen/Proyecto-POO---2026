package edu.uvg.gestionpedidos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String inicio() {
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    

}