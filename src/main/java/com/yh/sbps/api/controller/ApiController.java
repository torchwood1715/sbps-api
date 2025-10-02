package com.yh.sbps.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

  @GetMapping("/ping")
  public String ping() {
    return "API Service is up!";
  }
}
