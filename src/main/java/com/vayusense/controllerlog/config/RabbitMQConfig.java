package com.vayusense.controllerlog.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;

@Configuration
@EnableRabbit
public class RabbitMQConfig implements RabbitListenerConfigurer {
    @Value("${spring.rabbitmq.addresses}")
    private String addresses;

    @Value("${spring.rabbit.controllerlog.cachesize}")
    private int cacheSize;

    @Value("${app1.queue.name}")
    private String queueName1;

    @Value("${app1.exchange.name}")
    private String exchange1;

    @Value("${app1.routing.key}")
    private String routingkey1;


    @Bean
    public Queue getApp1Queue() {
        return new Queue(queueName1, false);
    }

    @Bean
    public DirectExchange getApp1exchange() {
        return new DirectExchange(exchange1);
    }

    @Bean
    public Binding app1binding() {
        return BindingBuilder.bind(getApp1Queue()).to(getApp1exchange()).with(routingkey1);
    }

    @Bean
    public MappingJackson2MessageConverter consumerJackson2MessageConverter() {
        return new MappingJackson2MessageConverter();
    }

    @Bean
    public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setMessageConverter(consumerJackson2MessageConverter());
        return factory;
    }


    @Bean
    public RabbitListenerContainerFactory<SimpleMessageListenerContainer> rabbitListenerContainerlog() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(5);
        factory.setRecoveryInterval(30000L);
        factory.setAdviceChain(
                RetryInterceptorBuilder
                        .stateless()
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .retryOperations(retryTemplate())
                        .build()
        );
        return factory;
    }

   @Bean
   public CachingConnectionFactory connectionFactory() {
		CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
		cachingConnectionFactory.setUri(addresses);
		cachingConnectionFactory.setChannelCacheSize(cacheSize);
        cachingConnectionFactory.setConnectionTimeout(30000);
        cachingConnectionFactory.getRabbitConnectionFactory().setAutomaticRecoveryEnabled(true);
        cachingConnectionFactory.getRabbitConnectionFactory().setNetworkRecoveryInterval(30000);
		return cachingConnectionFactory;
	}

    @Override
    public void configureRabbitListeners(final RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());

    }

    //retry
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(backOffPolicy());
        retryTemplate.setRetryPolicy(retryPolicy());
        return retryTemplate;
    }

    @Bean
    public ExponentialBackOffPolicy backOffPolicy() {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(30000);
        backOffPolicy.setMaxInterval(60000);
        return backOffPolicy;
    }

    @Bean
    public SimpleRetryPolicy retryPolicy() {
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(50);
        return retryPolicy;
    }



}