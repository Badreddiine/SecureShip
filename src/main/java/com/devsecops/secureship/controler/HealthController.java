
package com.devsecops.secureship.controler;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/live")
    public Map<String, String> live() {

        return Map.of("status", "UP");
    }

    @GetMapping("/ready")
    public Map<String, String> ready() {

        return Map.of("status", "UP");
    }

    @GetMapping("/version")
    public Map<String, String> version() {

        return Map.of("version", "2.0.0");
    }
}
