package com.weiyi.zhumao.config;

import com.weiyi.zhumao.service.TaskManagerService;
import com.weiyi.zhumao.service.impl.SimpleTaskManagerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {
    @Autowired
    AppProperties appProperties;

    @Bean
    public TaskManagerService taskManagerService(){
        return new SimpleTaskManagerService(appProperties.getCorePoolSize());
    }

    public int getNumsInRoom(){
        return appProperties.getNumsInRoom();
    }
}