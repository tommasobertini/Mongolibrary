package it.unipi.dii.inginf.lsmdb.mongolibrary.controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CustomErrorController implements ErrorController {
/*
    @RequestMapping("/error")
    @ResponseBody
    String error(HttpServletRequest request){
        return "redirect:/index";
    }

    /*@Override
    public String getErrorPath() {
        return "/error";
    }*/
}
