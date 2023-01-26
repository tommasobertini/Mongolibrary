package it.unipi.dii.inginf.lsmdb.mongolibrary.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NavController {

    @GetMapping("/")
    public String index(){
        return "index";
    }
/*
    @PostMapping("/")
    public String indexBack(){
        return "index";
    }

    @GetMapping("/signup")
    public String signup(){
        return "signup";
    }
*/

}
