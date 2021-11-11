package com.hao.demo.integrationdemo;

import com.github.javafaker.Faker;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;

import java.util.function.Supplier;

@Configuration
@EnableIntegration
public class RabbitMqIntegrationFlowConfig {

    @Bean
    public AmqpAdmin amqpAdmin() {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory());
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("127.0.0.1", 5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        return connectionFactory;
    }

    @Bean
    Supplier<String> fakeName() {
        return () -> new Faker().name().fullName();
    }

    @Bean
    public IntegrationFlow toRabbitmqFlow(CachingConnectionFactory connectionFactory, AmqpAdmin amqpAdmin) {
        Queue queue = QueueBuilder
                .durable("ERROR_LOG_QUEUE")
                .build();
        amqpAdmin.declareQueue(queue);

        return IntegrationFlows.fromSupplier(fakeName(), c -> c.poller(Pollers.fixedRate(1000)))
                .handle(Amqp.outboundAdapter(getAmqpTemplate(connectionFactory)).routingKey("ERROR_LOG_QUEUE"))
                .get();
    }

    @Bean
    public IntegrationFlow fromRabbitMqFlow(CachingConnectionFactory connectionFactory, AmqpAdmin amqpAdmin) {
        Queue queue = QueueBuilder
                .durable("ERROR_LOG_QUEUE")
                .build();
        amqpAdmin.declareQueue(queue);

        final SimpleMessageListenerContainer listenerContainer =
                getListenerContainer(connectionFactory, queue, 1, 1);

        return IntegrationFlows.from(Amqp.inboundAdapter(listenerContainer))
                .channel("mq_test_channel")
                .get();
    }

    @Bean
    public IntegrationFlow printMessageOnly() {
        return IntegrationFlows.from("mq_test_channel")
                .handle(System.out::println)
                .get();
    }

    private SimpleMessageListenerContainer getListenerContainer(CachingConnectionFactory connectionFactory, Queue queue, int concurrentConsumers, int prefetchCount) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueues(queue);
        container.setConcurrentConsumers(concurrentConsumers);
        container.setPrefetchCount(prefetchCount);
        return container;
    }

    private AmqpTemplate getAmqpTemplate(CachingConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }
}
