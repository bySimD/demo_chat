package com.chatserver.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ChatController {
    @GetMapping("/")
    public @ResponseBody  String hello() {
        return "hello world";
    }

    @GetMapping("/chat")
    public String chat() {
        return "index";
    }


}
