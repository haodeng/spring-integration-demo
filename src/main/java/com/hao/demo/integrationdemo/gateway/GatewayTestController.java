package com.hao.demo.integrationdemo.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class GatewayTestController {
    private final GatewayConfig.MessagePublisher messagePublisher;

    public GatewayTestController(GatewayConfig.MessagePublisher messagePublisher) {
        this.messagePublisher = messagePublisher;
    }

    @GetMapping(value = "/test/{string}")
    public ResponseEntity<String> getUser(@PathVariable("string") String string) {
        if (new Random().nextBoolean()) {
            messagePublisher.toDemoChannel1(string);
        }
        else {
            messagePublisher.toDemoChannel2(string);
        }

        return new ResponseEntity(HttpStatus.OK);
    }
}
