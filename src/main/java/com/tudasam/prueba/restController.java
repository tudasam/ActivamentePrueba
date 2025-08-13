package com.tudasam.prueba;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class restController {

    @GetMapping("/sendmessage")
    public String sendMessage(@RequestParam String message){
        return "success";
    }

}
