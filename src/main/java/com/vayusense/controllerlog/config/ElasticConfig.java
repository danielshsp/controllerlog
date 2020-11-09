package com.vayusense.controllerlog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;


@Configuration
@EnableReactiveElasticsearchRepositories(basePackages = "com.vayusense.controllerlog.persistence")
public class ElasticConfig {
}
