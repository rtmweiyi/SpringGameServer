package com.weiyi.zhumao;

import java.util.concurrent.TimeUnit;

import com.weiyi.zhumao.game.CheckMatchTask;
import com.weiyi.zhumao.game.CustomMatchmaker;

// import com.google.gson.Gson;

import com.weiyi.zhumao.server.ServerManager;
import com.weiyi.zhumao.service.TaskManagerService;

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
	@Autowired
	CustomMatchmaker customMatchMaker;
	@Autowired
	TaskManagerService taskManagerService;


	@SuppressWarnings({ "Convert2Lambda", "java:S1604" })
	@Bean
	public ApplicationListener<ApplicationEvent> readyApplicationListener() {
		return new ApplicationListener<ApplicationEvent>() {
			@Override
			public void onApplicationEvent(ApplicationEvent applicationReadyEvent) {
				try {
					//检查游戏队列任务,每30秒检查一下是否一直有人等待
					taskManagerService.scheduleAtFixedRate(new CheckMatchTask(customMatchMaker), 0, 10, TimeUnit.SECONDS);
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
