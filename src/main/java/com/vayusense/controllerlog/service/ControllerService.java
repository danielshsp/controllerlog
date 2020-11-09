package com.vayusense.controllerlog.service;

import com.vayusense.controllerlog.entities.ControllerLog;
import com.vayusense.controllerlog.persistence.ControllerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class ControllerService {

    private final ControllerRepository controllerRepository;

    public Mono<ControllerLog> saveControllerLog(ControllerLog controllerLog) {
         return controllerRepository.save(controllerLog).doOnError(e -> {
            log.error("find an error in controller log ",e);
        }).log();

    }


}
