package com.hao.demo.integrationdemo.route;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.router.PayloadTypeRouter;

import java.util.Random;
import java.util.function.Supplier;

@Configuration
@EnableIntegration
public class RoutingConfig {

    public RoutingConfig(ApplicationContext applicationContext) {
        ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();

        IntegrationFlow channel1 = channel1();
        beanFactory.registerSingleton("channel1Name", channel1);
        beanFactory.initializeBean(channel1, "channel1Name");

        IntegrationFlow channel2 = channel2();
        beanFactory.registerSingleton("channel2Name", channel2);
        beanFactory.initializeBean(channel2, "channel2Name");
    }


    @Bean
    public IntegrationFlow router() {
        return IntegrationFlows.from("route_channel")
                .route(messageChannelRouter())
                .get();
    }

    @Bean
    public PayloadTypeRouter messageChannelRouter() {
        PayloadTypeRouter router = new PayloadTypeRouter();

        router.setChannelMapping(EventA.class.getName(), "channel1");
        router.setChannelMapping(EventB.class.getName(), "channel2");

        router.setResolutionRequired(true);
        router.setDefaultOutputChannelName("error_channel");
        return router;
    }

    public IntegrationFlow channel1() {
        return IntegrationFlows.from("channel1")
                .handle(x -> System.out.println("channel 1:" + x.getPayload()))
                .get();
    }

    public IntegrationFlow channel2() {
        return IntegrationFlows.from("channel2")
                .handle(x -> System.out.println("channel 2:" + x.getPayload()))
                .get();
    }

    static BaseEvent[] allEvents() {
        return new BaseEvent[] {new EventA(), new EventB()};
    }

    @Bean
    Supplier<BaseEvent> testEvent() {
        return () -> allEvents()[new Random().nextInt(2)];
    }

    @Bean
    public IntegrationFlow startRoutingFlow() {
        return IntegrationFlows.fromSupplier(testEvent(), c -> c.poller(Pollers.fixedRate(1000)))
                .channel("route_channel")
                .get();
    }
}
