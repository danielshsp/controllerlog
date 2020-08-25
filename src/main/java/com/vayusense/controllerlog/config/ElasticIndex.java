package com.vayusense.controllerlog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "elastic.indexname")
@Data
public class ElasticIndex {

    private String controller;

}
