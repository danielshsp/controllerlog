package com.vayusense.controllerlog.service;


import com.rabbitmq.client.Channel;
import com.vayusense.controllerlog.entities.ControllerLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerLog {

    private final ControllerService controllerService;

    @RabbitListener(containerFactory = "rabbitListenerContainerlog", queues="${app1.queue.name}")
    public void recievedControllerLog(@Payload ControllerLog controllerLog, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag)throws Exception{
        try{
                Mono<ControllerLog> monocontrollerLog = controllerService.saveControllerLog(controllerLog);
                log.info("Recieved Message From RabbitMQ for controller log : " + controllerLog);
                if (!monocontrollerLog.block().getBatchId().isBlank()){
                    channel.basicAck(tag,false);
                }
        }catch (Exception e){
            log.error("there is a error while recieved a Message From RabbitMQ for controller log : " + controllerLog);
            channel.basicReject(tag, true);
        }

    }

}