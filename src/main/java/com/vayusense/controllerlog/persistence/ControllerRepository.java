package com.vayusense.controllerlog.persistence;

import com.vayusense.controllerlog.entities.ControllerLog;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ControllerRepository extends ReactiveElasticsearchRepository<ControllerLog,String> {
}
