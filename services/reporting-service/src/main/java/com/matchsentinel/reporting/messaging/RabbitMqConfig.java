package com.matchsentinel.reporting.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public DirectExchange transactionExchange(@Value("${reporting.rabbit.transaction.exchange}") String exchangeName) {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public DirectExchange flaggedExchange(@Value("${reporting.rabbit.flagged.exchange}") String exchangeName) {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public DirectExchange caseExchange(@Value("${reporting.rabbit.case.exchange}") String exchangeName) {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public DirectExchange notificationExchange(@Value("${reporting.rabbit.notification.exchange}") String exchangeName) {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Queue transactionQueue(@Value("${reporting.rabbit.transaction.queue}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Queue flaggedQueue(@Value("${reporting.rabbit.flagged.queue}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Queue caseQueue(@Value("${reporting.rabbit.case.queue}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Queue notificationQueue(@Value("${reporting.rabbit.notification.queue}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding transactionBinding(
            Queue transactionQueue,
            DirectExchange transactionExchange,
            @Value("${reporting.rabbit.transaction.routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(transactionQueue).to(transactionExchange).with(routingKey);
    }

    @Bean
    public Binding flaggedBinding(
            Queue flaggedQueue,
            DirectExchange flaggedExchange,
            @Value("${reporting.rabbit.flagged.routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(flaggedQueue).to(flaggedExchange).with(routingKey);
    }

    @Bean
    public Binding caseBinding(
            Queue caseQueue,
            DirectExchange caseExchange,
            @Value("${reporting.rabbit.case.routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(caseQueue).to(caseExchange).with(routingKey);
    }

    @Bean
    public Binding notificationBinding(
            Queue notificationQueue,
            DirectExchange notificationExchange,
            @Value("${reporting.rabbit.notification.routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with(routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.matchsentinel.reporting.dto");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
