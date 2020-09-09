package com.weiyi.zhumao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "jdbc")
public class DBProperties {
    private String url;
    private String username;
    private String password;
}