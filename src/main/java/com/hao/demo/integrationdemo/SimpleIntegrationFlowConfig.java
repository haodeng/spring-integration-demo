package com.hao.demo.integrationdemo;

import com.github.javafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;

import java.util.function.Supplier;

@Configuration
@EnableIntegration
public class SimpleIntegrationFlowConfig {
    @Bean
    Supplier<String> fakeAddress() {
        return () -> new Faker().address().fullAddress();
    }

    @Bean
    public IntegrationFlow startFlow() {
        return IntegrationFlows.fromSupplier(fakeAddress(), c -> c.poller(Pollers.fixedRate(1000)))
                .channel("test_channel")
                .get();
    }

    @Bean
    public IntegrationFlow upperCase() {
        return IntegrationFlows.from("test_channel")
                .transform((String s) -> s.toUpperCase())
                .handle(x -> System.out.println(x.getPayload()))
                .get();
    }
}
