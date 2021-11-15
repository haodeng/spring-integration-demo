package com.hao.demo.integrationdemo.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;

@Configuration
@EnableIntegration
public class GatewayConfig {

    @MessagingGateway
    public interface MessagePublisher {
        @Gateway(requestChannel = "gateway_demo_channel_1")
        void toDemoChannel1(String message);

        @Gateway(requestChannel = "gateway_demo_channel_2")
        void toDemoChannel2(String message);
    }



    @Bean
    public IntegrationFlow demoChannel1Flow() {
        return IntegrationFlows.from("gateway_demo_channel_1")
                .handle(x -> System.out.println("demo channel 1:" + x.getPayload()))
                .get();
    }

    @Bean
    public IntegrationFlow demoChannel2Flow() {
        return IntegrationFlows.from("gateway_demo_channel_2")
                .handle(x -> System.out.println("demo channel 2:" + x.getPayload()))
                .get();
    }
}
