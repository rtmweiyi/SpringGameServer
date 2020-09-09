package com.weiyi.zhumao;

// import com.google.gson.Gson;

import com.weiyi.zhumao.server.ServerManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	@Autowired
	ServerManager serverManager;


	@SuppressWarnings({ "Convert2Lambda", "java:S1604" })
	@Bean
	public ApplicationListener<ApplicationEvent> readyApplicationListener() {
		return new ApplicationListener<ApplicationEvent>() {
			@Override
			public void onApplicationEvent(ApplicationEvent applicationReadyEvent) {
				try {
					serverManager.startServers();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}

	// @Bean
	// public Gson createGson(){
	// 	return new Gson();
	// }

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
