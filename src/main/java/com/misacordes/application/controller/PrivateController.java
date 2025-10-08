package com.misacordes.application.controller;

import lombok.RequiredArgsConstructor;
// import lombok.Value; // No utilizado
// import org.springframework.web.bind.annotation.GetMapping; // No utilizado
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class PrivateController {

    @PostMapping(value = "home")
    public String home() {
        return "private home";
    }
}
